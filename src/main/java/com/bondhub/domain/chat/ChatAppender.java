package com.bondhub.domain.chat;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Component
public class ChatAppender {

    private final ChatRepository chatRepository;
    private final ChatJdbcRepository chatJdbcRepository;

    public void append(List<Chat> chats) {
        chatRepository.saveAll(chats);
    }

    public void appendInBatch(List<Chat> chats) {
        chatJdbcRepository.saveAll(chats);
    }
}
