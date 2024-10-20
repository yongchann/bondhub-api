package com.bondhub.domain.chat;

import com.bondhub.service.analysis.ClaudeClient;
import com.bondhub.service.analysis.MaturityDateExtractor;
import com.bondhub.service.claude.ChatSeparationRequest;
import com.bondhub.service.claude.ChatSeparationResponse;
import com.bondhub.service.claude.SimpleMultiBondChat;
import com.bondhub.service.claude.SeparationResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Component
public class MultiBondChatProcessor {

    private final MaturityDateExtractor maturityDateExtractor;
    private final ClaudeClient claudeClient;
    private final MultiBondChatFinder multiBondChatFinder;

    public List<Chat> separate(MultiBondChat multiBondChat, List<String> singleBondContents) {
        List<Chat> singleBondChats = new ArrayList<>();
        for (String content : singleBondContents) {
            List<String> maturityDates = maturityDateExtractor.extractAllMaturities(content);
            if (maturityDates.size() != 1) {
                throw new IllegalArgumentException("invalid split content, maturity date count must be 1, content:" + content);
            }

            singleBondChats.add(Chat.fromSeparation(multiBondChat, new ChatSeparationResult(content.trim(), maturityDates.get(0))));
        }

        multiBondChat.completeSeparation();
        return singleBondChats;
    }

    public List<Chat> autoSeparate(List<MultiBondChat> multiBondChats) {
        List<SimpleMultiBondChat> data = multiBondChats.stream().map(c -> new SimpleMultiBondChat(c.getId(), c.getContent())).toList();

        ChatSeparationResponse response = claudeClient.requestSeparation(new ChatSeparationRequest(data));

        List<Chat> separatedChats = new ArrayList<>();
        for (SeparationResult result : response.getMultiBondChats()) {
            MultiBondChat multiBondChat = multiBondChatFinder.getById(result.getId());
            List<String> contents = result.getContents();
            if (result.getContents().size() != multiBondChat.getMaturityDateCount()){
                multiBondChat.failSeparation();
                continue;
            }
            for (String content : contents) {
                List<String> maturityDates = maturityDateExtractor.extractAllMaturities(content);
                Chat chat = Chat.fromSeparation(multiBondChat, new ChatSeparationResult(content.trim(), maturityDates.get(0)));
                separatedChats.add(chat);
            }
            multiBondChat.completeSeparation();
        }

        return separatedChats;
    }
}
