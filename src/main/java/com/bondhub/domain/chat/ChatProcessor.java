package com.bondhub.domain.chat;

import com.bondhub.domain.ask.Bond;
import com.bondhub.service.dto.BondChatDto;
import com.bondhub.service.dto.ChatDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.*;

@RequiredArgsConstructor
@Component
public class ChatProcessor {

    public List<Chat> removeDuplication(List<Chat> allChats) {
        Map<String, Chat> uniqueChats = new HashMap<>();

        for (Chat chat : allChats) {
            String content = chat.getContent();
            if (!uniqueChats.containsKey(content) || chat.getChatDateTime().isAfter(uniqueChats.get(content).getChatDateTime())) {
                uniqueChats.put(content, chat);
            }
        }

        return new ArrayList<>(uniqueChats.values());
    }

    public List<BondChatDto> groupByBond(List<Chat> chats) {
        Map<Bond, BondChatDto> bondMap = new HashMap<>();
        for (Chat chat : chats) {
            Bond bond = new Bond(chat.getBondIssuer(), chat.getMaturityDate());

            bondMap.computeIfAbsent(bond, k -> BondChatDto.from(bond))
                    .getChats().add(ChatDto.builder()
                            .chatId(chat.getId())
                            .chatDateTime(chat.getChatDateTime())
                            .content(chat.getContent())
                            .build());
        }

        return bondMap.values().stream()
                .sorted(Comparator.comparing(BondChatDto::getMaturityDate))
                .toList();
    }
}
