package com.bbchat.service;

import com.bbchat.domain.bond.Bond;
import com.bbchat.domain.chat.Chat;
import com.bbchat.domain.chat.ChatStatus;
import com.bbchat.domain.chat.MultiBondChatHistory;
import com.bbchat.repository.MultiBondChatHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class ChatProcessor {

    private final ChatParser chatParser;
    private final BondClassifier bondClassifier;
    private final MultiBondChatHistoryRepository multiBondChatHistoryRepository;

    private static final List<String> SELLING_KEYWORDS = Arrays.asList("팔자", "매도", "매도 관심", "매도관심", "팔고", "팔거나", "추팔");
    private static final Map<String, String> REPLACEMENT_RULES = Map.of(
            "[부국채영]368-9532", "([부국채영]368-9532])",
            "(DS투자증권 채권전략팀 02)709-2701)", "(DS투자증권 채권전략팀 02-709-2701)",
            "[흥국채금 6260-2460)", "[흥국채금 6260-2460]",
            "김성훈(부국)", "김성훈부국",
            "\r\n", " ");

    @Transactional
    public List<Chat> processChatStr(String date, String rawContentChat) {
        List<Chat> allChats = chatParser.parseChatsFromRawText(date, rawContentChat)
                .stream().distinct().toList();

        // 모든 호가가 NOT_USED, SINGLE_DD, MULTI_DD 로 분류됨
        List<Chat> askChats = allChats.stream()
                .filter(chat -> isAskChat(chat.getContent()))
                .peek(chat -> chat.modifyStatusByDueDate(chatParser.extractDueDates(chat.getContent())))
                .collect(Collectors.toList());

        // 복수 종목 호가 분리 이력 조회
        Map<String, String> history = getMultiBondChatHistoryMap();
        askChats.stream()
                .filter(chat -> chat.getStatus().equals(ChatStatus.MULTI_DD))
                .forEach(multiBondChat -> {
                    String joinedContents = history.get(multiBondChat.getContent());
                    if (joinedContents != null) {
                        multiBondChat.setStatus(ChatStatus.SEPARATED);
                        List<String> splitContents = chatParser.splitJoinedContents(joinedContents);
                        askChats.addAll(chatParser.parseMultiBondChat(multiBondChat, splitContents));
                    }
                });

        // SINGLE_DD 에 대해 채권 할당
        askChats.stream()
                .filter(chat -> chat.getStatus().equals(ChatStatus.SINGLE_DD))
                .forEach(this::assignBondByContent);

        return allChats;
    }

    private Map<String, String> getMultiBondChatHistoryMap() {
        return multiBondChatHistoryRepository.findAll().stream()
                .collect(Collectors.toMap(
                        MultiBondChatHistory::getOriginalContent,
                        MultiBondChatHistory::getJoinedContents,
                        (existing, replacement) -> existing,
                        HashMap::new
                ));
    }

    public void assignBondByContent(Chat chat) {
        Bond bond = bondClassifier.extractBond(chat.getContent(), chat.getDueDate());
        if (bond == null) {
            chat.setStatus(ChatStatus.UNCATEGORIZED);
            log.warn("failed to extract bond from [%s]".formatted(chat.getContent()));
        } else {
            chat.setStatus(ChatStatus.OK);
            chat.setBond(bond);
        }
    }

    private boolean isAskChat(String content) {
        return SELLING_KEYWORDS.stream().anyMatch(content::contains);
    }

    private boolean isAllowedMessage(String content) {
        return bondClassifier.getExclusionKeywords().stream().noneMatch(content::contains);
    }

    public String preprocess(String rawText) {
        int index = rawText.indexOf("\r\n");
        rawText = rawText.substring(index + "\r\n".length());
        for (Map.Entry<String, String> entry : REPLACEMENT_RULES.entrySet()) {
            rawText = rawText.replace(entry.getKey(), entry.getValue());
        }

        return rawText;
    }

}
