package com.bondhub.domain.chat;

import com.bondhub.service.analysis.MaturityDateExtractor;
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

}
