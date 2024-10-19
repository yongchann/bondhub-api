package com.bondhub.domain.chat;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@RequiredArgsConstructor
@Component
public class MultiBondChatAppender {

    private final MultiBondChatRepository multiBondChatRepository;

    public void append(List<MultiBondChat> chat) {
        multiBondChatRepository.saveAll(chat);
    }

}
