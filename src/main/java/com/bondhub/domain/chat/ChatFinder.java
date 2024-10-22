package com.bondhub.domain.chat;

import com.bondhub.domain.bond.BondType;
import com.bondhub.service.dto.ChatGroupByContentDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Component
public class ChatFinder {

    private final ChatRepository chatRepository;

    public List<Chat> findDailyLatestSellChats(String date, BondType bondType) {
        LocalDateTime start = LocalDateTime.of(LocalDate.parse(date), LocalTime.MIN);
        LocalDateTime end = LocalDateTime.of(LocalDate.parse(date), LocalTime.MAX);
        return chatRepository.findLatestAsksByBond(start, end, bondType);
    }

    public List<Chat> findDailyCreditSellChats(String date, BondType bondType) {
        LocalDateTime start = LocalDateTime.of(LocalDate.parse(date), LocalTime.MIN);
        LocalDateTime end = LocalDateTime.of(LocalDate.parse(date), LocalTime.MAX);
        List<Chat> allChats = chatRepository.findByChatDateTimeBetweenAndBondIssuerType(start, end, bondType);

        return allChats.stream()
                .collect(Collectors.groupingBy(Chat::getContent,
                        Collectors.maxBy(Comparator.comparing(Chat::getChatDateTime))))
                .values()
                .stream()
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    public List<Chat> findDailyByStatus(String date, ChatStatus status) {
        LocalDateTime start = LocalDateTime.of(LocalDate.parse(date), LocalTime.MIN);
        LocalDateTime end = LocalDateTime.of(LocalDate.parse(date), LocalTime.MAX);
        return chatRepository.findByChatDateTimeBetweenAndStatus(start, end, status);
    }

    public List<ChatGroupByContentDto> findDailyByStatusGroupByContent(String date, ChatStatus status) {
        List<Chat> chats = findDailyByStatus(date, status);
        Map<String, List<Chat>> groupedByContent = chats.stream().collect(Collectors.groupingBy(Chat::getContent));

        List<ChatGroupByContentDto> result = new ArrayList<>();
        groupedByContent.forEach((content, value) -> {
            List<Long> ids = value.stream().map(Chat::getId).toList();
            result.add(new ChatGroupByContentDto(content, ids));
        });

        result.sort((c1, c2) -> Integer.compare(c2.getIds().size(), c1.getIds().size()));
        return result;
    }

    public List<Chat> findDailyInIds(String date, List<Long> targetIds) {
        LocalDateTime start = LocalDateTime.of(LocalDate.parse(date), LocalTime.MIN);
        LocalDateTime end = LocalDateTime.of(LocalDate.parse(date), LocalTime.MAX);
        return chatRepository.findByChatDateTimeBetweenAndIdIn(start, end, targetIds);
    }
}

