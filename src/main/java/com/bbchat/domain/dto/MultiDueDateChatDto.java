package com.bbchat.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor
@Builder
@Getter
public class MultiDueDateChatDto {

    private Long id;

    private String content;

}
