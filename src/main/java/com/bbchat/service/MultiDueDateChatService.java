package com.bbchat.service;

import com.bbchat.domain.dto.MultiDueDateChatDto;
import com.bbchat.domain.entity.ChatStatus;
import com.bbchat.domain.entity.MultiDueDateChat;
import com.bbchat.repository.MultiDueDateChatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class MultiDueDateChatService {

    private final MultiDueDateChatRepository multiDueDateChatRepository;

    public List<MultiDueDateChatDto> getByDate(String date) {
        List<MultiDueDateChat> chats = multiDueDateChatRepository.findByChatCreatedDate(date);
        return  chats.stream()
                .filter(chat -> chat.getStatus().equals(ChatStatus.CREATED))
                .map(chat -> MultiDueDateChatDto.builder()
                        .id(chat.getId())
                        .content(chat.getContent())
                        .chatCreatedDate(chat.getChatCreatedDate())
                        .senderName(chat.getSenderName())
                        .senderAddress(chat.getSenderAddress())
                        .sendDateTime(chat.getSendDateTime())
                        .build())
                .toList();
    }

}
