package com.otcbridge.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor
@Builder
@Getter
public class MultiDueDateChatDto {

    private Long id;

    private String content;

    private String chatCreatedDate;

    private String senderName;

    private String sendDateTime;

    private String senderAddress;

}
