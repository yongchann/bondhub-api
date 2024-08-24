package com.bbchat.controller.v1.response;

import com.bbchat.domain.Chat;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
@Getter
public class MultiDueDateChatsResponse {

    List<Chat> chats;
}
