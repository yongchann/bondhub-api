package com.bondhub.service.analysis;

import com.bondhub.domain.chat.Chat;
import com.bondhub.domain.chat.ChatStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@RequiredArgsConstructor
@Component
public class ChatParser {

    private static final String CHAT_MESSAGE_SEARCH_PATTERN = "([A-Za-z\\.가-힣0-9 女]+) \\((\\d{2}:\\d{2}:\\d{2})\\) :\\s*(.*?)(?=(?:[A-Z0-9a-z\\.가-힣 女]+\\s\\(\\d{2}:\\d{2}:\\d{2}\\)|$))";
    private static final String SENDER_ADDRESS_PATTERN = "\\s*\\([^)]*\\)\\s*$|\\s*\\[[^]]*\\]\\s*$|\\s*\\{[^}]*\\}\\s*$|\\s*<[^>]*>\\s*$|\\s*▨[^▨]*▨\\s*$|\\s*【[^】]*】\\s*$";
    public final static String CHAT_SPLIT_DELIMITER = "§";

    private static final Map<String, String> REPLACEMENT_RULES = Map.of(
            "[부국채영]368-9532", "([부국채영]368-9532])",
            "(DS투자증권 채권전략팀 02)709-2701)", "(DS투자증권 채권전략팀 02-709-2701)",
            "[흥국채금 6260-2460)", "[흥국채금 6260-2460]",
            "김성훈(부국)", "김성훈부국",
            "\r\n", " ");

    private final MaturityDateExtractor maturityDateExtractor;

    public List<Chat> parseChatStr(String chatDate, String rawContentChat) {
        Pattern pattern = Pattern.compile(CHAT_MESSAGE_SEARCH_PATTERN);
        Matcher matcher = pattern.matcher(rawContentChat);

        List<Chat> chats = new ArrayList<>();
        while (matcher.find()) {
            String senderName = matcher.group(1).trim();
            String sendTime = matcher.group(2);
            String content = matcher.group(3);
            String senderAddress = extractSenderAddress(content).trim();
            content = content.replace(senderAddress, "").trim();
            chats.add(Chat.builder()
                    .chatDateTime(LocalDateTime.of(LocalDate.parse(chatDate), LocalTime.parse(sendTime)))
                    .senderName(senderName)
                    .content(content)
                    .senderAddress(senderAddress)
                    .status(ChatStatus.CREATED)
                    .maturityDate("")
                    .build());
        }

        return removeDuplication(chats);
    }

    public String extractSenderAddress(String content) {
        Pattern pattern = Pattern.compile(SENDER_ADDRESS_PATTERN);
        Matcher matcher = pattern.matcher(content);
        return matcher.find() ? matcher.group(0).trim() : "";
    }

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

    public String preprocess(String rawText) {
        int index = rawText.indexOf("\r\n");
        rawText = rawText.substring(index + "\r\n".length());
        for (Map.Entry<String, String> entry : REPLACEMENT_RULES.entrySet()) {
            rawText = rawText.replace(entry.getKey(), entry.getValue());
        }

        return rawText;
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
