package com.otcbridge.controller.v1.request;

import com.otcbridge.domain.chat.ChatStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
@Getter
public class DiscardChatsRequest {

    private List<Long> chatIds;

    private ChatStatus status;

    private String chatDate;

}
