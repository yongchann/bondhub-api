package com.bondhub.service.dto;

import com.bondhub.domain.chat.Chat;
import com.bondhub.domain.chat.ChatStatus;
import com.bondhub.domain.chat.TradeType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Objects;

@Builder
@Getter
@AllArgsConstructor
public class ChatDto {

    private Long chatId;

    private String chatDate;

    private LocalDateTime chatDateTime;

    private String senderName;

    private String sendTime;

    private String content;

    private String senderAddress;

    public static Chat toEntity(ChatDto chat) {
        return Chat.builder()
                .status(ChatStatus.CREATED)
                .content(chat.getContent())
                .chatDateTime(LocalDateTime.of(LocalDate.parse(chat.getChatDate()), LocalTime.parse(chat.getSendTime())))
                .senderName(chat.getSenderName())
                .senderAddress(chat.getSenderAddress())
                .tradeType(TradeType.UNCATEGORIZED)
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
