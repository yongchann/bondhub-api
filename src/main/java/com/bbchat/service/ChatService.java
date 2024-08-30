package com.bbchat.service;

import com.bbchat.domain.chat.Chat;
import com.bbchat.domain.chat.ChatStatus;
import com.bbchat.repository.ChatRepository;
import com.bbchat.service.dto.ChatDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class ChatService {

    private final ChatRepository chatRepository;

    public List<String> findUncategorizedChats(String chatDate, String roomType) {
        List<Chat> chats = chatRepository.findByChatDateAndRoomTypeAndStatus(chatDate, roomType, ChatStatus.UNCATEGORIZED);
        return chats.stream().map(Chat::getContent).toList();
    }

    public List<ChatDto> findMultiBondChats(String chatDate, String roomType) {
        List<Chat> chats = chatRepository.findByChatDateAndRoomTypeAndStatus(chatDate, roomType, ChatStatus.MULTI_DD);
        return chats.stream().map(
                        chat -> ChatDto.builder()
                                .chatId(chat.getId())
                                .chatDate(chat.getChatDate())
                                .content(chat.getContent())
                                .senderName(chat.getSenderName())
                                .senderAddress(chat.getSenderAddress())
                                .sendTime(chat.getSendDateTime())
                                .build())
                .toList();
    }

}
