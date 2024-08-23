package com.bbchat.service;

import com.bbchat.domain.Chat;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RequiredArgsConstructor
@Component
public class ChatParser {

    private static final String CHAT_MESSAGE_SEARCH_PATTERN = "([A-Za-z\\.가-힣0-9 女]+) \\((\\d{2}:\\d{2}:\\d{2})\\) :\\s*(.*?)(?=(?:[A-Z0-9a-z\\.가-힣 女]+\\s\\(\\d{2}:\\d{2}:\\d{2}\\)|$))";
    private static final String SENDER_ADDRESS_PATTERN = "\\s*\\([^)]*\\)\\s*$|\\s*\\[[^]]*\\]\\s*$|\\s*\\{[^}]*\\}\\s*$|\\s*<[^>]*>\\s*$";
    private static final String VALID_DUE_DATE_PATTERN = "\\d{2}[./-]\\d{1,2}[./-]\\d{1,2}";

    public List<Chat> parseChatsFromRawText(String rawText) {
        Pattern pattern = Pattern.compile(CHAT_MESSAGE_SEARCH_PATTERN);
        Matcher matcher = pattern.matcher(rawText);

        List<Chat> chats = new ArrayList<>();
        while (matcher.find()) {
            String senderName = matcher.group(1).trim();
            String sendDateTime = matcher.group(2);
            String content = matcher.group(3);
            String senderAddress = extractSenderAddress(content).trim();
            content = content.replace(senderAddress, "").trim();

            chats.add(new Chat(senderName, sendDateTime,"", content, senderAddress));
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
        if (parts[1].length() == 1) parts[1] = "0" + parts[1];
        if (parts[2].length() == 1) parts[2] = "0" + parts[2];

        return String.join("-", parts);
    }
}
