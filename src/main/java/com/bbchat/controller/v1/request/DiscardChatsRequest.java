package com.bbchat.controller.v1.request;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
@Getter
public class DiscardChatsRequest {

    private List<Long> chatIds;

    private String chatDate;

}
