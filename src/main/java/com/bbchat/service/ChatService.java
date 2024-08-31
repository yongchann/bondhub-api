package com.bbchat.service;

import com.bbchat.domain.aggregation.ChatAggregation;
import com.bbchat.domain.chat.Chat;
import com.bbchat.domain.chat.ChatStatus;
import com.bbchat.repository.ChatAggregationRepository;
import com.bbchat.repository.ChatRepository;
import com.bbchat.service.dto.ChatDto;
import com.bbchat.service.exception.NotFoundAggregationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class ChatService {

    private final ChatRepository  chatRepository;
    private final ChatAggregationRepository chatAggregationRepository;
    private final ChatParser chatParser;
    private final ChatProcessor chatProcessor;

    public List<ChatDto> findUncategorizedChats(String chatDate, String roomType) {
        List<Chat> chats = chatRepository.findByChatDateAndRoomTypeAndStatus(chatDate, roomType, ChatStatus.UNCATEGORIZED);
        return chats.stream().map(
                        chat -> ChatDto.builder()
                                .chatId(chat.getId())
                                .content(chat.getContent())
                                .build())
                .toList();
    }

    public List<ChatDto> findMultiBondChats(String chatDate, String roomType) {
        List<Chat> chats = chatRepository.findByChatDateAndRoomTypeAndStatus(chatDate, roomType, ChatStatus.MULTI_DD);
        return chats.stream().map(
                        chat -> ChatDto.builder()
                                .chatId(chat.getId())
                                .content(chat.getContent())
                                .build())
                .toList();
    }

    @Transactional
    public int split(Long chatId, String chatDate, String roomType, List<String> splitContents) {
        Chat multiBondChat = chatRepository.findByIdAndChatDateAndRoomTypeAndStatus(chatId, chatDate, roomType, ChatStatus.MULTI_DD)
                .orElseThrow(() -> new NoSuchElementException("not found multi bond chat, chatId: "+ chatId + " in roomType: " + roomType));

        // 개별 채팅으로 분리
        List<Chat> separatedChats = chatParser.parseMultiBondChat(multiBondChat, splitContents);

        // 개별 채팅에 대해 분류 및 기존 채팅 상태 변경
        multiBondChat.setStatus(ChatStatus.SEPARATED);
        separatedChats.forEach(chatProcessor::assignBondByContent);
        chatRepository.saveAll(separatedChats);

        ChatAggregation aggregation = chatAggregationRepository.findTopByChatDateAndRoomTypeOrderByResultAggregatedDateTimeDesc(chatDate, roomType)
                .orElseThrow(() -> new NotFoundAggregationException("not found chat aggregation of " + chatDate));


        Map<ChatStatus, Long> statusCounts = separatedChats.stream()
                .collect(Collectors.groupingBy(Chat::getStatus, Collectors.counting()));

        aggregation.getResult().updateMultiDueDateSeparation(
                statusCounts.getOrDefault(ChatStatus.UNCATEGORIZED, 0L), // 미분류 채팅 수
                statusCounts.getOrDefault(ChatStatus.OK, 0L)); // 분류 채팅 수

        return separatedChats.size();
    }

    @Transactional
    public void discardChats(List<Long> chatIds, String chatDate, String roomType, ChatStatus targetStatus) {
        List<Chat> targetChats = chatRepository.findByChatDateAndRoomTypeAndStatusAndIdIn(chatDate, roomType, targetStatus, chatIds);
        if (chatIds.size() != targetChats.size()) {
            throw new IllegalArgumentException("Mismatch between requested and retrieved chat count.");
        }

        targetChats.forEach(chat -> chat.setStatus(ChatStatus.DISCARDED));
        ChatAggregation aggregation = chatAggregationRepository.findTopByChatDateAndRoomTypeOrderByResultAggregatedDateTimeDesc(chatDate, roomType)
                .orElseThrow(() -> new NotFoundAggregationException("not found chat aggregation of " + chatDate));

        if (targetStatus.equals(ChatStatus.MULTI_DD)) {
            aggregation.getResult().discardMultiDueDateChat(targetChats.size());
        } else if (targetStatus.equals(ChatStatus.UNCATEGORIZED)) {
            aggregation.getResult().discardUncategorizedChat(targetChats.size());
        } else {
            throw new IllegalArgumentException("can not discard this type of chat: " + targetChats);
        }
    }
}
