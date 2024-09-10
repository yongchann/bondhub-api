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
import com.otcbridge.service.dto.BondChatDto;
import com.otcbridge.service.dto.ChatDto;
import com.otcbridge.service.dto.ExclusionKeywordDto;
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

    public List<ChatDto> findUncategorizedChats(String chatDate) {
        List<Chat> chats = chatRepository.findByChatDateAndStatus(chatDate, ChatStatus.UNCATEGORIZED);
        return chats.stream().map(
                        chat -> ChatDto.builder()
                                .chatId(chat.getId())
                                .content(chat.getContent())
                                .build())
                .toList();
    }

    public List<ChatDto> findMultiBondChats(String chatDate) {
        List<Chat> chats = chatRepository.findByChatDateAndStatus(chatDate, ChatStatus.MULTI_DD);
        return chats.stream().map(
                        chat -> ChatDto.builder()
                                .chatId(chat.getId())
                                .content(chat.getContent())
                                .build())
                .toList();
    }

    @Transactional
    public int split(Long chatId, String chatDate, List<String> singleBondContents) {
        Chat multiBondChat = chatRepository.findByIdAndChatDateAndStatus(chatId, chatDate, ChatStatus.MULTI_DD)
                .orElseThrow(() -> new NoSuchElementException("not found multi bond chat, chatId: "+ chatId));

        // 개별 채팅으로 분리
        List<Chat> separatedChats = chatParser.parseMultiBondChat(multiBondChat, singleBondContents);

        // 개별 채팅에 대해 분류 및 기존 채팅 상태 변경
        multiBondChat.setStatus(ChatStatus.SEPARATED);
        separatedChats.forEach(chatProcessor::assignBondByContent);
        chatRepository.saveAll(separatedChats);

        multiBondChatHistoryRepository.save(new MultiBondChatHistory(chatDate, multiBondChat.getContent(), String.join("§", singleBondContents)));

        ChatAggregation aggregation = chatAggregationRepository.findTopByChatDateOrderByCreatedDateDesc(chatDate)
                .orElseThrow(() -> new NotFoundAggregationException("not found chat aggregation of " + chatDate));


        Map<ChatStatus, Long> statusCounts = separatedChats.stream()
                .collect(Collectors.groupingBy(Chat::getStatus, Collectors.counting()));

        aggregation.updateMultiDueDateSeparation(
                statusCounts.getOrDefault(ChatStatus.UNCATEGORIZED, 0L), // 미분류 채팅 수
                statusCounts.getOrDefault(ChatStatus.OK, 0L)); // 분류 채팅 수

        return separatedChats.size();
    }

    @Transactional
    public void discardChats(List<Long> chatIds, String chatDate, ChatStatus targetStatus) {
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
            bondMap.computeIfAbsent(chat.getBond(), k -> BondChatDto.from(chat.getBond())).getChats()
                    .add(ChatDto.builder()
                            .chatId(chat.getId())
                            .sendTime(chat.getSendDateTime())
                            .content(chat.getContent())
                            .build());
        }

        for (BondChatDto bondChatDto : bondMap.values()) {
            bondChatDto.sortChats();
        }

        return bondMap.values().stream()
                .sorted(Comparator.comparing(BondChatDto::getDueDate))
                .toList();
    }


}
