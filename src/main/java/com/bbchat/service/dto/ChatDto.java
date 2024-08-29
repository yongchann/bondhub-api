package com.bbchat.service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Objects;

@Getter
@AllArgsConstructor
public class ChatDto {

    private String sendTime;

    private String content;

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
