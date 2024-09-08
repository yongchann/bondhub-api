package com.bbchat.service;

import com.bbchat.domain.chat.Chat;
import com.bbchat.domain.chat.ChatStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@RequiredArgsConstructor
@Component
public class ChatParser {

    private static final String CHAT_MESSAGE_SEARCH_PATTERN = "([A-Za-z\\.가-힣0-9 女]+) \\((\\d{2}:\\d{2}:\\d{2})\\) :\\s*(.*?)(?=(?:[A-Z0-9a-z\\.가-힣 女]+\\s\\(\\d{2}:\\d{2}:\\d{2}\\)|$))";
    private static final String SENDER_ADDRESS_PATTERN = "\\s*\\([^)]*\\)\\s*$|\\s*\\[[^]]*\\]\\s*$|\\s*\\{[^}]*\\}\\s*$|\\s*<[^>]*>\\s*$|\\s*▨[^▨]*▨\\s*$|\\s*【[^】]*】\\s*$";
    private static final String VALID_DUE_DATE_PATTERN = "\\d{2}[./]\\d{1,2}[./]\\d{1,2}";
    private final static String CHAT_SPLIT_DELIMITER = "§";

    public List<Chat> parseChatsFromRawText(String chatDate, String rawText) {
        Pattern pattern = Pattern.compile(CHAT_MESSAGE_SEARCH_PATTERN);
        Matcher matcher = pattern.matcher(rawText);

        List<Chat> chats = new ArrayList<>();
        while (matcher.find()) {
            String senderName = matcher.group(1).trim();
            String sendDateTime = matcher.group(2);
            String content = matcher.group(3);
            String senderAddress = extractSenderAddress(content).trim();
            if (senderAddress.isEmpty()) {
                log.warn("[parseChatsFromRawText] senderAddress is empty, content: {}", content);
            }

            content = content.replace(senderAddress, "").trim();

            if (senderName.length() > 8) {
                log.warn("[parseChatsFromRawText] length of senderName is over 8: {}", senderName);
            }
            if (content.length() < 4) {
                continue;
            }

            chats.add(Chat.builder()
                    .chatDate(chatDate)
                    .senderName(senderName)
                    .sendDateTime(sendDateTime)
                    .content(content)
                    .senderAddress(senderAddress)
                    .status(ChatStatus.CREATED)
                    .dueDate("")
                    .build());
        }
        return chats;
    }

    public String extractSenderAddress(String content) {
        Pattern pattern = Pattern.compile(SENDER_ADDRESS_PATTERN);
        Matcher matcher = pattern.matcher(content);
        return matcher.find() ? matcher.group(0).trim() : "";
    }

    public List<String> extractDueDates(String content) {
        Matcher matcher = Pattern.compile(VALID_DUE_DATE_PATTERN).matcher(content);
        List<String> dates = new ArrayList<>();
        while (matcher.find()) {
            dates.add(standardizeDueDate(matcher.group()));
        }
        return dates;
    }

    private String standardizeDueDate(String date) {
        date = date.replace('.', '-').replace('/', '-');

        String[] parts = date.split("-");
        if (parts[0].length() == 2) parts[0] = "20" + parts[0];
        if (parts[1].length() == 1) parts[1] = "0" + parts[1];
        if (parts[2].length() == 1) parts[2] = "0" + parts[2];

        return String.join("-", parts);
    }

    public List<Chat> parseMultiBondChat(Chat multiBondChat, List<String> singleBondContents) {
        List<Chat> singleBondChats = new ArrayList<>();
        for (String singleBondContent : singleBondContents) {
            if (!multiBondChat.getContent().contains(singleBondContent)) {
                throw new IllegalArgumentException("invalid split content, org chat doesn't contain " + singleBondContent);
            }

            List<String> dueDates = extractDueDates(singleBondContent);
            if (dueDates.size() != 1) {
                throw new IllegalArgumentException("invalid split content, due date count must be 1, content:" + singleBondContent);
            }

            String dueDate = dueDates.get(0);
            singleBondChats.add(Chat.fromMultiBondChat(multiBondChat, singleBondContent, dueDate));
        }

        return singleBondChats;
    }

    public String joinContents(List<String> splitContents) {
        return String.join(CHAT_SPLIT_DELIMITER, splitContents);
    }

    public List<String> splitJoinedContents(String joinedContents) {
        return Arrays.stream(joinedContents.split(CHAT_SPLIT_DELIMITER)).toList();
    }
}
