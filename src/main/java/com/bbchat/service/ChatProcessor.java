package com.bbchat.service;

import com.bbchat.domain.Chat;
import com.bbchat.domain.ChatValidityType;
import com.bbchat.domain.entity.Ask;
import com.bbchat.domain.entity.Bond;
import com.bbchat.support.FileInfo;
import com.bbchat.support.S3FileRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Component
public class ChatProcessor {

    private final S3FileRepository s3FileRepository;
    private final ObjectMapper objectMapper;
    private final ChatParser chatParser;
    private final ChatProcessingRules data;

    public List<Ask> process(String date) {
        FileInfo chatFile = s3FileRepository.getChatFileByDate(date);

        List<Chat> allChats = chatParser.parseChatsFromRawText(preprocess(chatFile.getContent()));
        List<Chat> filteredAskChats = filterChats(allChats);

        Map<ChatValidityType, List<Chat>> chatsByValidity = classifyByDueDateCount(filteredAskChats);
        chatsByValidity.forEach((validityType, chats) -> {
            String fileName = String.format("%s/%s.json", date, validityType);
            saveChats(fileName, chats);
        });

        return extractAsksFromChats(chatsByValidity.get(ChatValidityType.VALID_SINGLE_DUE_DATE));
    }

    private List<Chat> filterChats(List<Chat> chats) {
        return chats.stream()
                .filter(chat -> isSellingMessage(chat.getContent()))
                .filter(chat -> isAllowedMessage(chat.getContent()))
                .collect(Collectors.toList());
    }

    private boolean isSellingMessage(String content) {
        List<String> askKeywords = data.getAskKeywords();
        return askKeywords.stream().anyMatch(content::contains);
    }

    private boolean isAllowedMessage(String content) {
        List<String> exclusionKeywords = data.getExclusionKeywords();
        return exclusionKeywords.stream().noneMatch(content::contains);
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

    private List<Ask> extractAsksFromChats(List<Chat> validChats) {
        Set<Ask> asks = new HashSet<>();
        Map<String, Bond> aliasToBondMap = data.getAliasToBondMap();

        validChats.forEach(chat -> aliasToBondMap.forEach((key, bond) -> {
            if (chat.getContent().contains(key)) {
                Ask ask = Ask.builder()
                        .bond(bond)
                        .triggerTerm(key)
                        .dueDate(chat.getDueDate())
                        .originalContent(chat.getContent())
                        .build();
                asks.add(ask);
            }
        }));
        return new ArrayList<>(asks);
    }

    private void saveChats(String filename, List<Chat> chats) {
        try {
            s3FileRepository.saveChatFile(filename, convertToInputStream(chats), MediaType.APPLICATION_JSON_VALUE);
        } catch (Exception e) {
            throw new RuntimeException("Error saving chats for filename: " + filename, e);
        }
    }

    public InputStream convertToInputStream(Object o)  {
        String jsonString = null;
        try {
            jsonString = objectMapper.writeValueAsString(o);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return new ByteArrayInputStream(jsonString.getBytes());
    }

    private String preprocess(String rawText) {
        // 첫줄 제거
        int index = rawText.indexOf("\r\n");
        rawText = rawText.substring(index + "\r\n".length());
        Map<String, String> rules = data.getReplacementRules();
        for (Map.Entry<String, String> entry : rules.entrySet()) {
            rawText = rawText.replace(entry.getKey(),entry.getValue());
        }

        return rawText;
    }

}
