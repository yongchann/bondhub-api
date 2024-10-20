package com.bondhub.domain.chat;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@RequiredArgsConstructor
@Component
public class MultiBondChatFinder {

    private final MultiBondChatRepository multiBondChatRepository;

    public MultiBondChat getById(Long id) {
        return multiBondChatRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 채팅입니다."));
    }

    public Optional<MultiBondChat> find(String date, String senderName, String content) {
        LocalDateTime start = LocalDateTime.of(LocalDate.parse(date), LocalTime.MIN);
        LocalDateTime end = LocalDateTime.of(LocalDate.parse(date), LocalTime.MAX);
        return multiBondChatRepository.findByChatDateTimeBetweenAndSenderNameAndContent(start, end, senderName, content);
    }

    public List<MultiBondChat> findDailyByStatus(String date, ChatStatus status, int limit) {
        LocalDateTime start = LocalDateTime.of(LocalDate.parse(date), LocalTime.MIN);
        LocalDateTime end = LocalDateTime.of(LocalDate.parse(date), LocalTime.MAX);
        return multiBondChatRepository.findByChatDateTimeBetweenAndStatus(start, end, status, Limit.of(limit));
    }

}
