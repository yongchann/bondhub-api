package com.bondhub.service.dto;

import com.bondhub.domain.chat.Chat;
import com.bondhub.domain.chat.ChatStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Objects;

@Builder
@Getter
@AllArgsConstructor
public class ChatDto {

    private Long chatId;

    private LocalDateTime chatDateTime;

    private String senderName;

    private String content;

    private String senderAddress;

    public static Chat toEntity(ChatDto dto) {
        return Chat.builder()
                .content(dto.getContent())
                .chatDateTime(dto.chatDateTime)
                .status(ChatStatus.CREATED)
                .senderName(dto.getSenderName())
                .senderAddress(dto.getSenderAddress())
                .build();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChatDto chatDto = (ChatDto) o;
        return Objects.equals(chatDateTime, chatDto.chatDateTime) && Objects.equals(content, chatDto.content);
    }

    @Override
    public int hashCode() {
        return Objects.hash(chatDateTime, content);
    }
}
