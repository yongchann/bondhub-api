package com.otcbridge.controller.v1.request;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
@Getter
public class AppendRecentChatRequest {

    private String chatDate;

    private List<RecentChatDto> recentChats;

    @Getter
    @AllArgsConstructor
    public static class RecentChatDto {

        private String senderName;

        private String sendTime;

        private String content;

        private String senderAddress;
    }
}
