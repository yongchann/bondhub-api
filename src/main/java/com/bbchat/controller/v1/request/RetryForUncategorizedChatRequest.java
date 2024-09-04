package com.bbchat.controller.v1.request;


import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class RetryForUncategorizedChatRequest {
    private String date;
    private String roomType;
}
