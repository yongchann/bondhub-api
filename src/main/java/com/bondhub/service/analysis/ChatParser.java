package com.bondhub.service.analysis;

import com.bondhub.domain.chat.Chat;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Component
public class ChatParser {

    public final static String CHAT_SPLIT_DELIMITER = "§";
    private final MaturityDateExtractor maturityDateExtractor;

    public List<Chat> parseMultiBondChat(Chat multiBondChat, List<String> singleBondContents) {
        List<Chat> singleBondChats = new ArrayList<>();
        for (String singleBondContent : singleBondContents) {
            singleBondContent = singleBondContent.trim();
            if (!multiBondChat.getContent().contains(singleBondContent)) {
                throw new IllegalArgumentException("invalid split content, org chat doesn't contain " + singleBondContent);
            }

            List<String> maturityDates = maturityDateExtractor.extractAllMaturities(singleBondContent);
            if (maturityDates.size() != 1) {
                throw new IllegalArgumentException("invalid split content, due date count must be 1, content:" + singleBondContent);
            }

            String maturityDate = maturityDates.get(0);
            singleBondChats.add(Chat.fromMultiBondChat(multiBondChat, singleBondContent, maturityDate));
        }

        return singleBondChats;
    }

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

}
