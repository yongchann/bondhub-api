package com.bondhub.domain.chat;

import com.bondhub.domain.bond.BondType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Component
public class ChatFinder {

    private final ChatRepository chatRepository;

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
}

