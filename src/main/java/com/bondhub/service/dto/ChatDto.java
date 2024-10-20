package com.bondhub.service.dto;

import com.bondhub.domain.chat.Chat;
import com.bondhub.domain.chat.ChatStatus;
import com.bondhub.domain.chat.MultiBondChat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
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

    public static Chat toChatEntity(ChatDto dto, List<String> maturityDates) {
        String maturityDate = "";
        if (!maturityDates.isEmpty()) {
            maturityDate = maturityDates.get(0);
        }

        return Chat.builder()
                .content(dto.getContent())
                .chatDateTime(dto.chatDateTime)
                .status(ChatStatus.CREATED)
                .senderName(dto.getSenderName())
                .senderAddress(dto.getSenderAddress())
                .maturityDate(maturityDate)
                .build();
    }

    public static MultiBondChat toMultiBondChatEntity(ChatDto dto, List<String> maturityDates) {
        return MultiBondChat.builder()
                .content(dto.getContent())
                .chatDateTime(dto.chatDateTime)
                .status(ChatStatus.NEEDS_SEPARATION)
                .senderName(dto.getSenderName())
                .senderAddress(dto.getSenderAddress())
                .maturityDateCount(maturityDates.size())
                .maturityDate(String.join(",", maturityDates))
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
