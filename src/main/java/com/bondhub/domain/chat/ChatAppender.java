package com.bondhub.domain.chat;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@RequiredArgsConstructor
@Component
public class ChatAppender {

    private final ChatRepository chatRepository;

    public void append(List<Chat> chats) {
        chatRepository.saveAll(chats);
    }

}
