package com.bbchat.controller.v1.request;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
@Getter
public class SplitMultiBondChatRequest {

    private Long chatId;

    private String chatDate;

    private String roomType;

    private List<String> splitContents;

}
