package com.otcbridge.service;

import com.otcbridge.domain.aggregation.ChatAggregation;
import com.otcbridge.domain.bond.Bond;
import com.otcbridge.domain.chat.Chat;
import com.otcbridge.domain.chat.ChatStatus;
import com.otcbridge.domain.chat.ExclusionKeyword;
import com.otcbridge.domain.chat.MultiBondChatHistory;
import com.otcbridge.repository.ChatAggregationRepository;
import com.otcbridge.repository.ChatRepository;
import com.otcbridge.repository.ExclusionKeywordRepository;
import com.otcbridge.repository.MultiBondChatHistoryRepository;
import com.otcbridge.service.dto.*;
import com.otcbridge.service.event.ExclusionKeywordEvent;
import com.otcbridge.service.exception.NotFoundAggregationException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class ChatService {

    private final ChatParser chatParser;
    private final ChatProcessor chatProcessor;

    private final ChatRepository  chatRepository;
    private final ChatAggregationRepository chatAggregationRepository;
    private final ExclusionKeywordRepository exclusionKeywordRepository;
    private final MultiBondChatHistoryRepository multiBondChatHistoryRepository;

    private final ApplicationEventPublisher publisher;

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
        ChatAggregation aggregation = chatAggregationRepository.findTopByChatDateOrderByCreatedDateDesc(chatDate)
                .orElseThrow(() -> new NotFoundAggregationException("not found chat aggregation of " + chatDate));

        if (targetStatus.equals(ChatStatus.MULTI_DD)) {
            aggregation.discardMultiDueDateChat(targetChats.size());
        } else if (targetStatus.equals(ChatStatus.UNCATEGORIZED)) {
            aggregation.discardUncategorizedChat(targetChats.size());
        } else {
            throw new IllegalArgumentException("can not discard this type of chat: " + targetChats);
        }
    }
    public List<ExclusionKeywordDto> getExclusionKeywords() {
        List<ExclusionKeyword> keywords = exclusionKeywordRepository.findAll();
        return  keywords.stream()
                .map(keyword -> new ExclusionKeywordDto(keyword.getId(), keyword.getName()))
                .toList();
    }

    @Transactional
    public void deleteExclusionKeywords(Long exclusionKeywordId) {
        ExclusionKeyword exclusionKeyword = exclusionKeywordRepository.findById(exclusionKeywordId)
                .orElseThrow(() -> new IllegalArgumentException("not found exclusion keyword, id: " + exclusionKeywordId));

        exclusionKeywordRepository.delete(exclusionKeyword);

        publisher.publishEvent(new ExclusionKeywordEvent(this, ExclusionKeywordEvent.Type.DELETED, "exclusion keyword deleted", exclusionKeyword.getName()));
    }

    public String createExclusionKeyword(String name) {
        Optional<ExclusionKeyword> keyword = exclusionKeywordRepository.findByName(name);
        if (keyword.isPresent()) {
            throw new IllegalArgumentException("already exist exclusion keyword name : " + name);
        }

        exclusionKeywordRepository.save(new ExclusionKeyword(name));

        publisher.publishEvent(new ExclusionKeywordEvent(this, ExclusionKeywordEvent.Type.CREATED, "exclusion keyword created" ,name));

        return name;
    }

    @Transactional
    public List<BondChatDto> retryForUncategorizedChat(String chatDate) {
        List<Chat> uncategorizedChats = chatRepository.findByChatDateAndStatus(chatDate, ChatStatus.UNCATEGORIZED);

        // 재분류
        uncategorizedChats.forEach(chatProcessor::assignBondByContent);

        // 집계 업데이트를 위한 조회
        ChatAggregation aggregation = chatAggregationRepository.findTopByChatDateOrderByCreatedDateDesc(chatDate)
                .orElseThrow(() -> new NotFoundAggregationException("not found chat aggregation of " + chatDate));

        List<Chat> successChats = uncategorizedChats.stream()
                .filter(chat -> chat.getStatus().equals(ChatStatus.OK))
                .toList();

        aggregation.updateRetrialOfUncategorizedChat(successChats.size());

        return groupByBond(successChats);
    }
    private List<BondChatDto> groupByBond(List<Chat> chats) {
        Map<Bond, BondChatDto> bondMap = new HashMap<>();
        for (Chat chat : chats) {
            bondMap.computeIfAbsent(chat.getBond(), k -> BondChatDto.from(chat.getBond()))
                    .getChats().add(ChatDto.builder()
                            .chatId(chat.getId())
                            .sendTime(chat.getSendDateTime())
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
}
