package com.bondhub.service;

import com.bondhub.domain.aggregation.ChatAggregation;
import com.bondhub.domain.aggregation.ChatAggregationRepository;
import com.bondhub.domain.ask.Bond;
import com.bondhub.domain.chat.*;
import com.bondhub.service.analysis.ChatAnalyzer;
import com.bondhub.service.analysis.ChatParser;
import com.bondhub.service.dto.BondChatDto;
import com.bondhub.service.dto.ChatDto;
import com.bondhub.service.dto.ChatGroupByContentDto;
import com.bondhub.service.exception.NotFoundAggregationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

import static com.bondhub.service.analysis.ChatParser.CHAT_SPLIT_DELIMITER;

@Slf4j
@RequiredArgsConstructor
@Service
public class ChatService {

    private final ChatParser chatParser;
    private final ChatAnalyzer chatAnalyzer;
    private final ChatAppender chatAppender;
    private final ChatFinder chatFinder;
    private final ChatAggregationRepository chatAggregationRepository;
    private final MultiBondChatHistoryRepository multiBondChatHistoryRepository;


    public List<ChatGroupByContentDto> getChatsGroupByContent(String chatDate, ChatStatus status) {
        List<Chat> chats = chatFinder.findDailyByStatus(chatDate, status);

        Map<String, List<Chat>> groupedByContent = chats.stream().collect(Collectors.groupingBy(Chat::getContent));

        List<ChatGroupByContentDto> result = new ArrayList<>();
        groupedByContent.forEach((content, value) -> {
            List<Long> ids = value.stream().map(Chat::getId).toList();
            result.add(new ChatGroupByContentDto(content, ids));
        });

        result.sort((c1, c2) -> Integer.compare(c2.getIds().size(), c1.getIds().size()));
        return result;
    }

    @Transactional
    public int split(String chatDate, List<Long> targetIds, String originalContent, List<String> singleBondContents) {
        List<Chat> multiBondChats = chatFinder.findDailyByStatus(chatDate, ChatStatus.NEEDS_SEPARATION, targetIds);
        if (multiBondChats.isEmpty()) {
            throw new IllegalArgumentException("대상 복수 종목 호가가 올바르지 않습니다.");
        }
        if (multiBondChats.stream().anyMatch(chat -> !Objects.equals(chat.getContent(), originalContent))) {
            throw new IllegalArgumentException("조회된 복수 종목 호가의 내용이 올바르지 않습니다.");
        }

        // 복수 종목 호가를 n 개로 분리
        List<Chat> separatedChats = chatParser.parseMultiBondChat(multiBondChats.get(0), singleBondContents);

        // 개별 채팅에 대해 분류 및 기존 채팅 상태 변경
        multiBondChats.forEach(chat ->  chat.setStatus(ChatStatus.SEPARATED));
        separatedChats.forEach(chatAnalyzer::analyze);
        chatAppender.append(separatedChats);

        // 분리 이력 추가
        String joinedContents = String.join(CHAT_SPLIT_DELIMITER, separatedChats.stream().map(Chat::getContent).toList());
        multiBondChatHistoryRepository.save(new MultiBondChatHistory(chatDate, originalContent, joinedContents));

        // 집계 업데이트
        ChatAggregation aggregation = chatAggregationRepository.findByChatDateWithPessimisticLock(chatDate)
                .orElseThrow(() -> new NotFoundAggregationException("not found chat aggregation of " + chatDate));

        Map<ChatStatus, Long> statusCounts = separatedChats.stream()
                .collect(Collectors.groupingBy(Chat::getStatus, Collectors.counting()));

        aggregation.updateMultiDueDateSeparation(
                multiBondChats.size(),
                statusCounts.getOrDefault(ChatStatus.UNCATEGORIZED, 0L), // 미분류 채팅 수
                statusCounts.getOrDefault(ChatStatus.OK, 0L)); // 분류 채팅 수

        return separatedChats.size();
    }

    @Transactional
    public void discardChats(String chatDate, ChatStatus targetStatus, List<Long> chatIds) {
        List<Chat> targetChats = chatFinder.findDailyByStatus(chatDate, targetStatus, chatIds);
        if (chatIds.size() != targetChats.size()) {
            throw new IllegalArgumentException("Mismatch between requested and retrieved chat count.");
        }

        targetChats.forEach(chat -> chat.setStatus(ChatStatus.DISCARDED));
        ChatAggregation aggregation = chatAggregationRepository.findByChatDateWithPessimisticLock(chatDate)
                .orElseThrow(() -> new NotFoundAggregationException("not found chat aggregation of " + chatDate));

        if (targetStatus.equals(ChatStatus.NEEDS_SEPARATION)) {
            aggregation.discardMultiDueDateChat(targetChats.size());
        } else if (targetStatus.equals(ChatStatus.UNCATEGORIZED)) {
            aggregation.discardUncategorizedChat(targetChats.size());
        } else {
            throw new IllegalArgumentException("can not discard this tradeType of chat: " + targetChats);
        }
    }

    private List<BondChatDto> groupByBond(List<Chat> chats) {
        Map<Bond, BondChatDto> bondMap = new HashMap<>();
        for (Chat chat : chats) {
            Bond bond = new Bond(chat.getBondIssuer(), chat.getMaturityDate());

            bondMap.computeIfAbsent(bond, k -> BondChatDto.from(bond))
                    .getChats().add(ChatDto.builder()
                            .chatId(chat.getId())
                            .chatDateTime(chat.getChatDateTime())
                            .content(chat.getContent())
                            .build());
        }

        return bondMap.values().stream()
                .sorted(Comparator.comparing(BondChatDto::getMaturityDate))
                .toList();
    }
    @Transactional
    public void append(String chatDate, List<ChatDto> newChats) {
        List<Chat> chats = newChats.stream().map(ChatDto::toEntity).toList();

        chats.forEach(chatAnalyzer::analyze);

        chatAppender.append(chats);

        // 채팅 가공 상태에 따른 집계 결과를 생성
        Map<ChatStatus, Long> statusCounts = chats.stream()
                .collect(Collectors.groupingBy(Chat::getStatus, Collectors.counting()));

        ChatAggregation chatAggregation = chatAggregationRepository.findByChatDateWithPessimisticLock(chatDate)
                .orElseGet(() -> chatAggregationRepository.save(ChatAggregation.create(chatDate)));

        chatAggregation.update(statusCounts);
    }

    @Transactional
    public List<BondChatDto> retryForUncategorizedChat(String chatDate) {
        List<Chat> uncategorizedChats = chatFinder.findDailyByStatus(chatDate, ChatStatus.UNCATEGORIZED);

        // 재분류
        uncategorizedChats.forEach(chatAnalyzer::analyze);

        // 집계 업데이트를 위한 조회
        ChatAggregation aggregation = chatAggregationRepository.findByChatDateWithPessimisticLock(chatDate)
                .orElseThrow(() -> new NotFoundAggregationException("not found chat aggregation of " + chatDate));

        List<Chat> successChats = uncategorizedChats.stream()
                .filter(chat -> chat.getStatus().equals(ChatStatus.OK))
                .toList();

        aggregation.updateRetrialOfUncategorizedChat(successChats.size());

        return groupByBond(successChats);
    }

}
