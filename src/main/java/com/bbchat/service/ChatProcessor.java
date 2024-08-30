package com.bbchat.service;

import com.bbchat.domain.aggregation.ChatAggregationResult;
import com.bbchat.domain.bond.Bond;
import com.bbchat.domain.chat.Chat;
import com.bbchat.domain.chat.ChatStatus;
import com.bbchat.repository.ChatRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class ChatProcessor {

    private final ChatParser chatParser;
    private final BondClassifier bondClassifier;
    private final ChatRepository chatRepository;

    private final Map<String, String> replacementRules = Map.of(
            "[부국채영]368-9532", "([부국채영]368-9532])",
            "(DS투자증권 채권전략팀 02)709-2701)", "(DS투자증권 채권전략팀 02-709-2701)",
            "[흥국채금 6260-2460)", "[흥국채금 6260-2460]",
            "김성훈(부국)", "김성훈부국",
            "\r\n", " ");

    @Transactional
    public ChatAggregationResult aggregateFromRawContent(String date, String rawContentChat, String roomType) {
        chatRepository.deleteAllByChatDateAndRoomType(date,roomType);

        List<Chat> allChats = chatParser.parseChatsFromRawText(date, preprocess(rawContentChat), roomType);

        List<Chat> filteredChats = allChats.stream()
                .filter(chat -> isSellingMessage(chat.getContent()))
                .filter(chat -> isAllowedMessage(chat.getContent())) // TODO 국고채도 집계하고자 하는 경우 수정 필요
                .peek(chat -> chat.modifyStatusByDueDate(chatParser.extractDueDates(chat.getContent())))
                .toList();
        chatRepository.saveAll(filteredChats);

        List<Chat> singleDueDateChats = filteredChats.stream()
                .filter(chat -> chat.getStatus().equals(ChatStatus.SINGLE_DD))
                .toList();

        singleDueDateChats.forEach(chat -> {
            Bond bond = bondClassifier.extractBond(chat.getContent(), chat.getDueDate());
            if (bond == null) {
                chat.setStatus(ChatStatus.UNCATEGORIZED);
                log.warn("failed to extract bond from [%s]".formatted(chat.getContent()));
            } else {
                chat.setStatus(ChatStatus.OK);
                chat.setBond(bond);
            }
        });

        Map<ChatStatus, Long> statusCounts = allChats.stream()
                .collect(Collectors.groupingBy(Chat::getStatus, Collectors.counting()));

        return ChatAggregationResult.builder()
                .totalChatCount(allChats.size())
                .excludedChatCount(allChats.size() - filteredChats.size())
                .notUsedChatCount(statusCounts.get(ChatStatus.NOT_USED))
                .multiDueDateChatCount(statusCounts.get(ChatStatus.MULTI_DD))
                .uncategorizedChatCount(statusCounts.get(ChatStatus.UNCATEGORIZED))
                .fullyProcessedChatCount(statusCounts.get(ChatStatus.OK))
                .build();
    }

    private boolean isSellingMessage(String content) {
        return content.contains("팔자");
    }

    private boolean isAllowedMessage(String content) {
        return bondClassifier.getExclusionKeywords().stream().noneMatch(content::contains);
    }

    private String preprocess(String rawText) {
        int index = rawText.indexOf("\r\n");
        rawText = rawText.substring(index + "\r\n".length());
        for (Map.Entry<String, String> entry : replacementRules.entrySet()) {
            rawText = rawText.replace(entry.getKey(), entry.getValue());
        }

        return rawText;
    }

}
