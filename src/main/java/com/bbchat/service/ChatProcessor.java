package com.bbchat.service;

import com.bbchat.domain.Chat;
import com.bbchat.domain.ChatValidityType;
import com.bbchat.domain.entity.*;
import com.bbchat.repository.BondRepository;
import com.bbchat.repository.DailyAskRepository;
import com.bbchat.repository.MultiDueDateChatRepository;
import com.bbchat.support.FileInfo;
import com.bbchat.support.S3FileRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Component
public class ChatProcessor {

    private final S3FileRepository s3FileRepository;
    private final BondRepository bondRepository;
    private final DailyAskRepository dailyAskRepository;
    private final MultiDueDateChatRepository multiDueDateChatRepository;

    private final ObjectMapper objectMapper;
    private final ChatParser chatParser;
    private final ChatProcessingRules rules;

    public void process(String date) {
        log.info("Processing chats for date: {}", date);

        FileInfo chatFile = s3FileRepository.getChatFileByDate(date);
        log.info("Retrieved chat file for date: {}", date);

        List<Chat> allChats = parseAndPreprocessChats(chatFile.getContent());
        log.info("Parsed and preprocessed {} chats", allChats.size());

        List<Chat> filteredChats = filterChats(allChats);
        log.info("Filtered chats, remaining count: {}", filteredChats.size());

        // 만기일 포함 갯수에 따른 분류
        Map<ChatValidityType, List<Chat>> chatsByDueDateCount = classifyByDueDateCount(filteredChats);
        log.info("Classified chats by due date count");

        saveClassifiedChats(chatsByDueDateCount, date);
        log.info("Saved classified chats of {} to S3", date);

        List<Chat> validChats = chatsByDueDateCount.get(ChatValidityType.VALID_SINGLE_DUE_DATE);
        log.info("Processing valid chats with single due date, count: {}", validChats.size());
        
        List<Chat> validMultiDueDateChats = chatsByDueDateCount.get(ChatValidityType.VALID_MULTI_DUE_DATE);
        saveMultiDueDateChats(date, validMultiDueDateChats);
        log.info("Saved valid chats with multi due date, count: {}", validMultiDueDateChats.size());

        int dailyAsksSize = categorize(date, validChats);
        log.info("Processed {} DailyAsk entities", dailyAsksSize);
    }

    @Transactional
    private int categorize(String date, List<Chat> validChats) {
        List<DailyAsk> dailyAsks = processValidChats(validChats, date);
        dailyAskRepository.saveAll(dailyAsks);
        return dailyAsks.size();
    }

    private List<Chat> parseAndPreprocessChats(String rawContent) {
        String preprocessedContent = preprocess(rawContent);
        return chatParser.parseChatsFromRawText(preprocessedContent);
    }

    private void saveClassifiedChats(Map<ChatValidityType, List<Chat>> chatsByValidity, String date) {
        chatsByValidity.forEach((validityType, chats) -> {
            String fileName = String.format("%s/%s.json", date, validityType);
            saveChats(fileName, chats);
        });
    }

    private LocalDate parseDate(String date) {
        String[] split = date.split("-");
        return LocalDate.of(Integer.parseInt(split[0]), Integer.parseInt(split[1]), Integer.parseInt(split[2]));
    }

    private List<DailyAsk> processValidChats(List<Chat> validChats, String todayStr) {
        LocalDate today = parseDate(todayStr);
        LocalDate yesterday = today.minusDays(1);

        Set<DailyAsk> result = new HashSet<>();
        Map<BondIssuer, List<String>> bondAliasesMap = rules.getBondAliasesMap();

        List<Chat> uncategorizedChats = new ArrayList<>();

        validChats.forEach(chat -> {
            AtomicBoolean matched = new AtomicBoolean(false);

            for (Map.Entry<BondIssuer, List<String>> entry : bondAliasesMap.entrySet()) {
                BondIssuer bondIssuer = entry.getKey();
                List<String> aliases = entry.getValue();
                for (String alias : aliases) {
                    if (chat.getContent().contains(alias)) {
                        Bond bond = findOrCreateBond(bondIssuer, chat.getDueDate());
                        int consecutiveDays = findOrCreateDailyAsk(bond, yesterday);

                        DailyAsk dailyAsk = createDailyAsk(bond, today, consecutiveDays);
                        result.add(dailyAsk);

                        matched.set(true);
                        break;
                    }
                }
                if (matched.get()) break;
            }
            if (!matched.get()) {
                uncategorizedChats.add(chat);
            }
        });

        saveChats(String.format("%s/%s.json", today, "UNCATEGORIZED"), uncategorizedChats);
        return new ArrayList<>(result);
    }

    private Bond findOrCreateBond(BondIssuer bondIssuer, String dueDate) {
        return bondRepository.findByBondIssuerAndDueDate(bondIssuer, dueDate)
                .orElseGet(() -> bondRepository.save(new Bond(bondIssuer, dueDate)));
    }

    private int findOrCreateDailyAsk(Bond bond, LocalDate yesterday) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yy-MM-dd");
        String yesterdayStr = yesterday.format(formatter);

        Optional<DailyAsk> optionalDailyAsk = dailyAskRepository.findByBondAndDate(bond, yesterdayStr);
        return optionalDailyAsk.map(DailyAsk::getConsecutiveDays).orElse(0) + 1;
    }

    private DailyAsk createDailyAsk(Bond bond, LocalDate today, int consecutiveDays) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yy-MM-dd");
        String todayStr = today.format(formatter);

        return DailyAsk.builder()
                .bond(bond)
                .createdDate(todayStr)
                .consecutiveDays(consecutiveDays)
                .build();
    }

    private List<Chat> filterChats(List<Chat> chats) {
        return chats.stream()
                .filter(chat -> isSellingMessage(chat.getContent()))
                .filter(chat -> isAllowedMessage(chat.getContent()))
                .collect(Collectors.toList());
    }

    private boolean isSellingMessage(String content) {
        return rules.getAskKeywords().stream().anyMatch(content::contains);
    }

    private boolean isAllowedMessage(String content) {
        return rules.getExclusionKeywords().stream().noneMatch(content::contains);
    }

    private Map<ChatValidityType, List<Chat>> classifyByDueDateCount(List<Chat> chats) {
        return chats.stream().collect(Collectors.groupingBy(chat -> {
            List<String> extractedDates = chatParser.extractDueDates(chat.getContent());

            if (extractedDates.isEmpty()) {
                return ChatValidityType.INVALID;
            } else if (extractedDates.size() > 1) {
                return ChatValidityType.VALID_MULTI_DUE_DATE;
            }

            chat.setDueDate(extractedDates.get(0));
            return ChatValidityType.VALID_SINGLE_DUE_DATE;
        }));
    }

    private void saveChats(String filename, Object chats) {
        try {
            s3FileRepository.saveChatFile(filename, convertToInputStream(chats), MediaType.APPLICATION_JSON_VALUE);
        } catch (Exception e) {
            throw new RuntimeException("Error saving chats for filename: " + filename, e);
        }
    }

    public InputStream convertToInputStream(Object o) {
        try {
            String jsonString = objectMapper.writeValueAsString(o);
            return new ByteArrayInputStream(jsonString.getBytes());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private String preprocess(String rawText) {
        int index = rawText.indexOf("\r\n");
        rawText = rawText.substring(index + "\r\n".length());
        Map<String, String> replacementRules = rules.getReplacementRules();
        for (Map.Entry<String, String> entry : replacementRules.entrySet()) {
            rawText = rawText.replace(entry.getKey(), entry.getValue());
        }

        return rawText;
    }

    private void saveMultiDueDateChats(String date, List<Chat> validMultiDueDateChats) {
        List<MultiDueDateChat> multiDueDateChatEntity = validMultiDueDateChats.stream()
                .map(chat -> MultiDueDateChat.builder()
                            .content(chat.getContent())
                            .chatCreatedDate(date)
                            .status(ChatStatus.CREATED)
                            .sendDateTime(chat.getSendDateTime())
                            .senderAddress(chat.getSenderAddress())
                            .senderName(chat.getSenderName())
                            .build()
                ).toList();
        multiDueDateChatRepository.saveAll(multiDueDateChatEntity);
    }
}
