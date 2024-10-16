package com.bondhub.domain.chat;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@RequiredArgsConstructor
@Component
public class ChatRemover {

    private final ChatRepository chatRepository;

    public int removeDailyAll(String date) {
        LocalDateTime start = LocalDateTime.of(LocalDate.parse(date), LocalTime.MIN);
        LocalDateTime end = LocalDateTime.of(LocalDate.parse(date), LocalTime.MAX);
        return chatRepository.deleteByChatDateTimeBetween(start, end);
    }
}

