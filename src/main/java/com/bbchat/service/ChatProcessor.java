package com.bbchat.service;

import com.bbchat.domain.aggregation.ChatAggregationResult;
import com.bbchat.domain.bond.Bond;
import com.bbchat.domain.chat.Chat;
import com.bbchat.domain.chat.ChatStatus;
import com.bbchat.domain.chat.MultiBondChatHistory;
import com.bbchat.repository.ChatRepository;
import com.bbchat.repository.MultiBondChatHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class ChatProcessor {

    private final ChatParser chatParser;
    private final BondClassifier bondClassifier;
    private final ChatRepository chatRepository;
    private final MultiBondChatHistoryRepository multiBondChatHistoryRepository;

    private final Map<String, String> replacementRules = Map.of(
            "[부국채영]368-9532", "([부국채영]368-9532])",
            "(DS투자증권 채권전략팀 02)709-2701)", "(DS투자증권 채권전략팀 02-709-2701)",
            "[흥국채금 6260-2460)", "[흥국채금 6260-2460]",
            "김성훈(부국)", "김성훈부국",
            "\r\n", " ");

    @Transactional
    public ChatAggregationResult aggregateFromRawContentWithOffset(String date, String rawContentChat, String roomType, int offset) {
        List<Chat> allChats = chatParser.parseChatsFromRawText(date, preprocess(rawContentChat), roomType);
        if (offset > allChats.size()) {
            throw new IllegalStateException("offset is greater than total chat size");
        }

        List<Chat> targetChats = allChats.subList(offset, allChats.size());

        // targetChats -> offset 이 적용된 대상 채팅들
        List<Chat> filteredChats = targetChats.stream()
                .filter(chat -> isSellingMessage(chat.getContent()))
                .filter(chat -> isAllowedMessage(chat.getContent())) // TODO 국고채도 집계하고자 하는 경우 수정 필요
                .peek(chat -> chat.modifyStatusByDueDate(chatParser.extractDueDates(chat.getContent())))
                .collect(Collectors.toList());

        long excludedChatCount = targetChats.size() - filteredChats.size();

        // 분리된 기록이 있는 복수 종목 호가를 재가공하여 추가
        List<Chat> sepChats = new ArrayList<>();
        filteredChats.stream()
                .filter(chat -> chat.getStatus().equals(ChatStatus.MULTI_DD))
                .forEach(chat -> {
                    List<Chat> separatedChats = findSeparationHistory(chat);
                    if (!separatedChats.isEmpty()) {
                        sepChats.addAll(separatedChats); // 분리해서 넣기
                        chat.setStatus(ChatStatus.SEPARATED); // 상태 변경
                    }
                });

        filteredChats.addAll(sepChats);

        filteredChats.stream()
                .filter(chat -> chat.getStatus().equals(ChatStatus.SINGLE_DD))
                .forEach(this::assignBondByContent);

        chatRepository.saveAll(filteredChats);

        Map<ChatStatus, Long> statusCounts = allChats.stream()
                .collect(Collectors.groupingBy(Chat::getStatus, Collectors.counting()));

        return ChatAggregationResult.builder()
                .aggregatedDateTime(LocalDateTime.now())
                .totalChatCount(allChats.size())
                .excludedChatCount(excludedChatCount)
                .notUsedChatCount(statusCounts.getOrDefault(ChatStatus.NOT_USED, 0L))
                .multiDueDateChatCount(statusCounts.getOrDefault(ChatStatus.MULTI_DD, 0L))
                .uncategorizedChatCount(statusCounts.getOrDefault(ChatStatus.UNCATEGORIZED, 0L))
                .fullyProcessedChatCount(statusCounts.getOrDefault(ChatStatus.OK, 0L))
                .build();
    }

    private List<Chat> findSeparationHistory(Chat originalChat) {
        Optional<MultiBondChatHistory> cache = multiBondChatHistoryRepository.findByOriginalContent(originalChat.getContent());
        if (cache.isEmpty()){
            return List.of();
        } else {
            String splitContents = cache.get().getSplitContents();
            List<String> splitContentList = Arrays.stream(splitContents.split("§")).toList();
            return chatParser.parseMultiBondChat(originalChat, splitContentList);
        }
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
