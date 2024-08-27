package com.bbchat.service;

import com.bbchat.domain.bond.Bond;
import com.bbchat.domain.entity.Chat;
import com.bbchat.domain.entity.ChatStatus;
import com.bbchat.domain.ask.DailyAsk;
import com.bbchat.repository.ChatRepository;
import com.bbchat.repository.DailyAskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class ChatProcessor {

    private final ChatParser chatParser;
    private final BondClassifier bondClassifier;
    private final ChatRepository chatRepository;

    private final DailyAskRepository dailyAskRepository;
    private final Map<String, String> replacementRules = Map.of(
            "[부국채영]368-9532", "([부국채영]368-9532])",
            "(DS투자증권 채권전략팀 02)709-2701)", "(DS투자증권 채권전략팀 02-709-2701)",
            "[흥국채금 6260-2460)", "[흥국채금 6260-2460]",
            "김성훈(부국)", "김성훈부국",
            "\r\n", " ");

    @Transactional
    public void processFromRawContent(String date, String rawContentChat) {
        chatRepository.deleteAllByChatDate(date);
        dailyAskRepository.deleteAllByCreatedDate(date);

        List<Chat> allChats = chatParser.parseChatsFromRawText(date, preprocess(rawContentChat));

        List<Chat> filteredChats = allChats.stream()
                .filter(chat -> isSellingMessage(chat.getContent()))
                .filter(chat -> isAllowedMessage(chat.getContent())) // TODO 국고채도 집계하고자 하는 경우 수정 필요
                .peek(chat -> chat.modifyStatusByDueDate(chatParser.extractDueDates(chat.getContent())))
                .toList();

        transform(date, filteredChats);
    }

    public void transform(String date, List<Chat> askChats) {
        Map<Chat, DailyAsk> chatDailyAskMap = mapChatToDailyAsk(date, askChats);
        Set<DailyAsk> dailyAsks = new HashSet<>(chatDailyAskMap.values());
        List<DailyAsk> persistedDailyAsks = dailyAskRepository.saveAll(new ArrayList<>(dailyAsks));

        Map<String, DailyAsk> dailyAskMap = persistedDailyAsks.stream()
                .collect(Collectors.toMap(da -> da.getBond().getId() + da.getCreatedDate(), da -> da));

        for (Chat chat : chatDailyAskMap.keySet()) {
            DailyAsk originalDailyAsk = chatDailyAskMap.get(chat);
            String key = originalDailyAsk.getBond().getId() + originalDailyAsk.getCreatedDate();
            chat.setDailyAsk(dailyAskMap.get(key));
        }

        chatRepository.saveAll(chatDailyAskMap.keySet());
    }

    private Map<Chat, DailyAsk> mapChatToDailyAsk(String date, List<Chat> filteredChats) {
        Map<Chat, DailyAsk> chatDailyAskMap = new HashMap<>();
        for (Chat chat : filteredChats) {
            if (chat.getStatus().equals(ChatStatus.SINGLE_DD)) {
                Bond bond = bondClassifier.extractBond(chat.getContent(), chat.getDueDate());
                if (bond == null) {
                    chat.setStatus(ChatStatus.UNCATEGORIZED);
                    log.warn("failed to extract bond from [%s]".formatted(chat.getContent()));
                    continue;
                }
                DailyAsk dailyAsk = DailyAsk.builder()
                        .bond(bond)
                        .createdDate(date)
                        .consecutiveDays(calculateConsecutiveDays(bond, date))
                        .build();

                chatDailyAskMap.put(chat, dailyAsk);
            }
        }
        return chatDailyAskMap;
    }

    private boolean isSellingMessage(String content) {
        return content.contains("팔자");
    }

    private boolean isAllowedMessage(String content) {
        return bondClassifier.getExclusionKeywords().stream().noneMatch(content::contains);
    }

    private String getYesterdayStr(String todayStr) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yy-MM-dd");
        LocalDate today = LocalDate.parse(todayStr, formatter);
        LocalDate yesterday = today.minusDays(1);
        return yesterday.format(formatter);
    }

    private int calculateConsecutiveDays(Bond bond, String today) {
        Optional<DailyAsk> optionalDailyAsk = dailyAskRepository.findByBondAndDate(bond, getYesterdayStr(today));
        return optionalDailyAsk.map(DailyAsk::getConsecutiveDays).orElse(0) + 1;
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
