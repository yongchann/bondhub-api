package com.bondhub.controller.v1.request;

import com.bondhub.service.dto.ChatDto;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
@Getter
public class AppendChatRequest {

    private String chatDate;

    private List<ChatDto> chats;

}
