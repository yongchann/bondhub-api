package com.bondhub.domain.chat;

import com.bondhub.domain.bond.BondType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@RequiredArgsConstructor
@Component
public class ChatReader {

    private final ChatRepository chatRepository;

    public List<Chat> getClassifiedChat(String chatDate, BondType bondType) {
        return chatRepository.findClassifiedChatWithBond(chatDate, bondType);
    }
}
