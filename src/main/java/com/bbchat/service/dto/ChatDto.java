package com.bbchat.service.dto;

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

    public ChatDto(String sendTime, String content) {
        this.sendTime = sendTime;
        this.content = content;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChatDto chatDto = (ChatDto) o;
        return Objects.equals(content, chatDto.content);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(content);
    }

}
