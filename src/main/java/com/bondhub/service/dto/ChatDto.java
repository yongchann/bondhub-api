package com.bondhub.service.dto;

import com.bondhub.domain.chat.Chat;
import com.bondhub.domain.chat.ChatStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.Objects;

@Builder
@Getter
@AllArgsConstructor
public class ChatDto {

    private Long chatId;

    private String chatDate;

    private String senderName;

    private String sendTime;

    private String content;

    private String senderAddress;

    public static Chat toEntity(ChatDto chat) {
        return Chat.builder()
                .status(ChatStatus.CREATED)
                .content(chat.getContent())
                .sendTime(chat.getSendTime())
                .senderName(chat.getSenderName())
                .senderAddress(chat.getSenderAddress())
                .build();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChatDto chatDto = (ChatDto) o;
        return Objects.equals(sendTime, chatDto.sendTime) && Objects.equals(content, chatDto.content);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sendTime, content);
    }
}
