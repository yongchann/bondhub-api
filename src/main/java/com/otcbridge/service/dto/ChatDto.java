package com.otcbridge.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

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

    @Setter
    private boolean containExclusionKeyword;

    public ChatDto(String sendTime, String content, boolean containExclusionKeyword) {
        this.sendTime = sendTime;
        this.content = content;
        this.containExclusionKeyword = containExclusionKeyword;
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