package com.bbchat.service;

import com.bbchat.domain.Chat;
import com.bbchat.domain.ChatValidityType;
import com.bbchat.domain.ClassificationData;
import com.bbchat.domain.entity.Ask;
import com.bbchat.support.ChatStreamConverter;
import com.bbchat.support.S3FileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Component
public class ChatProcessor {

    private static final String CHAT_MESSAGE_SEARCH_PATTERN = "([A-Za-z\\.가-힣0-9 女]+) \\((\\d{2}:\\d{2}:\\d{2})\\) :\\s*(.*?)(?=(?:[A-Z0-9a-z\\.가-힣 女]+\\s\\(\\d{2}:\\d{2}:\\d{2}\\)|$))";
    private static final String SENDER_ADDRESS_PATTERN = "\\s*\\([^)]*\\)\\s*$|\\s*\\[[^]]*\\]\\s*$|\\s*\\{[^}]*\\}\\s*$|\\s*<[^>]*>\\s*$";
    private static final String VALID_DUE_DATE_PATTERN = "\\d{2}[./-]\\d{1,2}[./-]\\d{1,2}";

    private final S3FileRepository s3FileRepository;
    private final ClassificationData classificationData;
    private final ChatStreamConverter converter;

    public List<Ask> process(String date, String rawText) {
        Pattern pattern = Pattern.compile(CHAT_MESSAGE_SEARCH_PATTERN);
        Matcher matcher = pattern.matcher(cleanRawText(rawText));

        // 채팅 메시지 객체화
        List<Chat> allChats = new ArrayList<>();
        while (matcher.find()) {
            String senderName = matcher.group(1).trim();
            String sendDateTime = matcher.group(2);
            String content = matcher.group(3);
            String senderAddress = extractSenderAddress(content).trim();
            content = content.replace(senderAddress, "").trim();

            allChats.add(new Chat(senderName, sendDateTime, null, content, senderAddress));
        }

        // 필터링
        List<Chat> askChats = allChats.stream()
                .filter(chat -> isSellingMessage(chat.getContent())) // 매도 호가만
                .filter(chat -> isTargetMessage(chat.getContent())) // 제외 키워드
                .toList();

        // 만기 포함 갯수에 따른 분류
        Map<ChatValidityType, List<Chat>> chatsByValidity = askChats.stream().collect(Collectors.groupingBy(chat -> {
            List<String> dates = extractDueDates(chat.getContent());
            if (dates.isEmpty()) {
                return ChatValidityType.INVALID;
            } else if (dates.size() > 1) {
                return ChatValidityType.VALID_MULTI_DUE_DATE;
            }
            chat.setDueDate(parseDueDate(dates.get(0)));
            return ChatValidityType.VALID_SINGLE_DUE_DATE;
        }));


        // 키워드 포함에 따른 종목 분류
        List<Chat> invalidChats = chatsByValidity.get(ChatValidityType.INVALID);
        try {
            s3FileRepository.saveChatFile(date+"/"+ChatValidityType.INVALID, converter.convertListToInputStream(invalidChats));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        List<Chat> probablyValidChats = chatsByValidity.get(ChatValidityType.VALID_MULTI_DUE_DATE);
        try {
            s3FileRepository.saveChatFile(date+"/"+ChatValidityType.VALID_MULTI_DUE_DATE, converter.convertListToInputStream(probablyValidChats));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        List<Chat> validChats = chatsByValidity.get(ChatValidityType.VALID_SINGLE_DUE_DATE);
        try {
            s3FileRepository.saveChatFile(date+"/"+ChatValidityType.VALID_SINGLE_DUE_DATE, converter.convertListToInputStream(validChats));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        List<Ask> asks = new ArrayList<>();
        validChats.forEach(chat -> {
            for (String key : classificationData.getBondAliasMap().keySet()) {
                if (chat.getContent().contains(key)) {
                    Ask ask = Ask.builder()
                            .bond(classificationData.getBondAliasMap().get(key))
                            .triggerTerm(key)
                            .dueDate(chat.getDueDate())
                            .originalContent(chat.getContent())
                            .build();
                    asks.add(ask);
                    break; // Stop once the first matching key is found
                }
            }
        });

        return asks;
    }

    private String cleanRawText(String rawText) {
        rawText = rawText.replace("\r\n", " ");
        rawText = rawText.replace("\n", " ");
        rawText = rawText.replace("[부국채영]368-9532", "([부국채영]368-9532])");
        rawText = rawText.replace("김성훈(부국)", "김성훈");
        return rawText;
    }

    private String extractSenderAddress(String content) {
        Pattern pattern = Pattern.compile(SENDER_ADDRESS_PATTERN);
        Matcher matcher = pattern.matcher(content);

        if (matcher.find()) {
            return matcher.group(0).trim();
        }
        return "";
    }

    private boolean isSellingMessage(String content) {
        return content.contains("팔자");
    }

    private boolean isTargetMessage(String content) {
        return classificationData.getExclusionKeywords().stream()
                .noneMatch(content::contains);
    }

    private List<String> extractDueDates(String content) {
        Pattern pattern = Pattern.compile(VALID_DUE_DATE_PATTERN);
        Matcher matcher = pattern.matcher(content);
        List<String> dates = new ArrayList<>();
        while (matcher.find()) {
            dates.add(matcher.group());
        }
        return dates;
    }

    private String parseDueDate(String date) {
        date = date.replace('.', '-').replace('/', '-');
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yy-MM-dd");
        try {
            return String.valueOf(LocalDate.parse(date, formatter));
        } catch (DateTimeParseException e) {
            return null;
        }
    }
}
