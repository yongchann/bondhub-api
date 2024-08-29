package com.bbchat.service;

import com.bbchat.domain.MaturityCondition;
import com.bbchat.domain.bond.Bond;
import com.bbchat.domain.entity.Chat;
import com.bbchat.repository.ChatRepository;
import com.bbchat.service.dto.ChatDto;
import com.bbchat.service.dto.DailyAskDto;
import com.bbchat.service.exception.IllegalInquiryParameterException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@RequiredArgsConstructor
@Service
public class AskService {

    private final ChatRepository chatRepository;

    public Map<Bond, DailyAskDto> inquiry(String date, MaturityCondition condition, List<String> grades) {
        String start, end;
        if (condition.getMaturityInquiryType().equals("remain")) {
            start = calculateDate(date, Integer.parseInt(condition.getMinMonth()));
            end = calculateDate(date, Integer.parseInt(condition.getMaxMonth()));
        } else if (condition.getMaturityInquiryType().equals("explicit")) {
            start = condition.getMinYear() + "-" + condition.getMinMonth() + "-01";
            end = condition.getMaxYear() + "-" + condition.getMaxMonth() + "-31";
        } else {
            throw new IllegalInquiryParameterException("maturityInquiryType: " + condition.getMaturityInquiryType());
        }

        // 조건에 맞는 채팅 조회
        List<Chat> chats = chatRepository.findValidChatsWithinDueDateRangeAndIssuerGrades(date, start, end, grades);

        // 채권에 따라 채팅을 grouping
        return groupByBond(chats);

    }

    private Map<Bond, DailyAskDto> groupByBond(List<Chat> chats) {
        Map<Bond, DailyAskDto> result = new TreeMap<>(Comparator.comparing(Bond::getDueDate));
        chats.forEach(chat -> {
            Bond key = chat.getDailyAsk().getBond();
            result.computeIfAbsent(key, k -> DailyAskDto.from(chat.getDailyAsk()))
                    .getChats()
                    .add(new ChatDto(chat.getSendDateTime(), chat.getContent()));
        });

        for (DailyAskDto dailyAskDto : result.values()) {
            dailyAskDto.sortChats();
        }
        return result;
    }


    private String calculateDate(String baseDate, int monthsToAdd) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate date = LocalDate.parse(baseDate, formatter);
        LocalDate calculatedDate = date.plusMonths(monthsToAdd);
        return calculatedDate.format(formatter);
    }
}
