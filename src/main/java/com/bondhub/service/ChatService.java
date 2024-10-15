package com.bondhub.service;

import com.bondhub.domain.bond.Bond;
import com.bondhub.domain.chat.*;
import com.bondhub.domain.common.FileInfo;
import com.bondhub.service.dto.BondChatDto;
import com.bondhub.service.dto.ChatDto;
import com.bondhub.service.dto.MultiBondChatDto;
import com.bondhub.service.dto.UncategorizedChatDto;
import com.bondhub.service.exception.NotFoundAggregationException;
import com.bondhub.support.S3FileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.bondhub.service.UploadService.CHAT_FILE_KEY_PREFIX;
import static com.bondhub.service.UploadService.CHAT_FILE_SAVE_NAME;
import static com.bondhub.support.S3FileRepository.buildPath;

@Slf4j
@RequiredArgsConstructor
@Service
public class ChatService {

    private final ChatParser chatParser;
    private final ChatProcessor chatProcessor;

    private final ChatRepository  chatRepository;
    private final ChatAggregationRepository chatAggregationRepository;
    private final MultiBondChatHistoryRepository multiBondChatHistoryRepository;

    private final S3FileRepository fileRepository;

    public List<UncategorizedChatDto> findUncategorizedChats(String chatDate) {
        List<Chat> chats = chatRepository.findByChatDateAndStatus(chatDate, ChatStatus.UNCATEGORIZED);
        Map<String, List<Chat>> groupedByContent = chats.stream().collect(Collectors.groupingBy(Chat::getContent));

        List<UncategorizedChatDto> result = new ArrayList<>();
        groupedByContent.forEach((content, value) -> {
            List<Long> ids = value.stream().map(Chat::getId).toList();
            result.add(new UncategorizedChatDto(content, ids));
        });

        result.sort((c1, c2) -> Integer.compare(c2.getIds().size(), c1.getIds().size()));
        return result;
    }

    public List<MultiBondChatDto> findMultiBondChats(String chatDate) {
        List<Chat> chats = chatRepository.findByChatDateAndStatus(chatDate, ChatStatus.MULTI_DD);
        Map<String, List<Chat>> groupedByContent = chats.stream().collect(Collectors.groupingBy(Chat::getContent));

        List<MultiBondChatDto> result = new ArrayList<>();
        groupedByContent.forEach((content, value) -> {
            List<Long> ids = value.stream().map(Chat::getId).toList();
            result.add(new MultiBondChatDto(content, ids));
        });

        result.sort((c1, c2) -> Integer.compare(c2.getIds().size(), c1.getIds().size()));
        return result;
    }

    @Transactional
    public int split(String chatDate, List<Long> targetIds, String originalContent, List<String> singleBondContents) {
        List<Chat> multiBondChats = chatRepository.findByChatDateAndStatusAndIdIn(chatDate, ChatStatus.MULTI_DD, targetIds);
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
        separatedChats.forEach(chatProcessor::assignBondByContent);
        chatRepository.saveAll(separatedChats);

        // 분리 이력 추가
        String joinedContents = chatParser.joinContents(separatedChats.stream().map(Chat::getContent).toList());
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
        List<Chat> targetChats = chatRepository.findByChatDateAndStatusAndIdIn(chatDate, targetStatus, chatIds);
        if (chatIds.size() != targetChats.size()) {
            throw new IllegalArgumentException("Mismatch between requested and retrieved chat count.");
        }

        targetChats.forEach(chat -> chat.setStatus(ChatStatus.DISCARDED));
        ChatAggregation aggregation = chatAggregationRepository.findByChatDateWithPessimisticLock(chatDate)
                .orElseThrow(() -> new NotFoundAggregationException("not found chat aggregation of " + chatDate));

        if (targetStatus.equals(ChatStatus.MULTI_DD)) {
            aggregation.discardMultiDueDateChat(targetChats.size());
        } else if (targetStatus.equals(ChatStatus.UNCATEGORIZED)) {
            aggregation.discardUncategorizedChat(targetChats.size());
        } else {
            throw new IllegalArgumentException("can not discard this type of chat: " + targetChats);
        }
    }

    private List<BondChatDto> groupByBond(List<Chat> chats) {
        Map<Bond, BondChatDto> bondMap = new HashMap<>();
        for (Chat chat : chats) {
            bondMap.computeIfAbsent(chat.getBond(), k -> BondChatDto.from(chat.getBond()))
                    .getChats().add(ChatDto.builder()
                            .chatId(chat.getId())
                            .sendTime(chat.getSendTime())
                            .content(chat.getContent())
                            .build());
        }

        return bondMap.values().stream()
                .sorted(Comparator.comparing(BondChatDto::getDueDate))
                .toList();
    }
    @Transactional
    public void append(String chatDate, List<ChatDto> recentChats) {
        List<Chat> chats = chatProcessor.convertToEntity(chatDate, recentChats);

        // 채팅 가공 상태에 따른 집계 결과를 생성
        Map<ChatStatus, Long> statusCounts = chats.stream()
                .collect(Collectors.groupingBy(Chat::getStatus, Collectors.counting()));

        ChatAggregation chatAggregation = chatAggregationRepository.findByChatDateWithPessimisticLock(chatDate)
                .orElseGet(() -> chatAggregationRepository.save(ChatAggregation.create(chatDate)));

        chatAggregation.update(statusCounts);
        chatRepository.saveAll(chats);
    }

    @Transactional
    public List<BondChatDto> retryForUncategorizedChat(String chatDate) {
        List<Chat> uncategorizedChats = chatRepository.findByChatDateAndStatus(chatDate, ChatStatus.UNCATEGORIZED);

        // 재분류
        uncategorizedChats.forEach(chatProcessor::assignBondByContent);

        // 집계 업데이트를 위한 조회
        ChatAggregation aggregation = chatAggregationRepository.findByChatDateWithPessimisticLock(chatDate)
                .orElseThrow(() -> new NotFoundAggregationException("not found chat aggregation of " + chatDate));

        List<Chat> successChats = uncategorizedChats.stream()
                .filter(chat -> chat.getStatus().equals(ChatStatus.OK))
                .toList();

        aggregation.updateRetrialOfUncategorizedChat(successChats.size());

        return groupByBond(successChats);
    }

    @Transactional
    public void reanalyzeAll(String date) {
        // 홰당 일자의 채팅을 모두 삭제
        int deletedCount = chatRepository.deleteAllByChatDateInBatch(date);
        log.info("[aggregateChat] deleted {} chats", deletedCount);

        // 3개의 채팅 데이터 조회 후 하나의 문자열로 병합
        String entireChatStr = Stream.of("BB", "RB", "MM")
                .map(type -> {
                    FileInfo chatFile = fileRepository.get(buildPath(CHAT_FILE_KEY_PREFIX, date, type), CHAT_FILE_SAVE_NAME);
                    return chatProcessor.preprocess(chatFile.getContent());
                })
                .collect(Collectors.joining());

        // 채팅 가공 후 모두 저장
        List<Chat> allChats = chatProcessor.processChatStr(date, entireChatStr);
        chatRepository.saveAll(allChats); // TODO JDBC Batch insert 적용
        log.info("[aggregateChat] created {} chats", allChats.size());

        // 채팅 가공 상태에 따른 집계 결과를 생성
        Map<ChatStatus, Long> statusCounts = allChats.stream()
                .collect(Collectors.groupingBy(Chat::getStatus, Collectors.counting()));

        ChatAggregation aggregation = ChatAggregation.builder()
                .chatDate(date)
                .totalChatCount(allChats.size())
                .notUsedChatCount(statusCounts.getOrDefault(ChatStatus.CREATED, 0L))
                .multiDueDateChatCount(statusCounts.getOrDefault(ChatStatus.MULTI_DD, 0L))
                .uncategorizedChatCount(statusCounts.getOrDefault(ChatStatus.UNCATEGORIZED, 0L))
                .fullyProcessedChatCount(statusCounts.getOrDefault(ChatStatus.OK, 0L))
                .build();

        chatAggregationRepository.save(aggregation);
    }
}
