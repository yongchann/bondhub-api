package com.bbchat.service;

import com.bbchat.domain.BondType;
import com.bbchat.domain.DailyAskSummary;
import com.bbchat.domain.dto.DailyAskDto;
import com.bbchat.domain.entity.DailyAsk;
import com.bbchat.repository.DailyAskRepository;
import com.bbchat.repository.MultiDueDateChatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class AggregationService {

    private final ChatProcessor chatProcessor;
    private final DailyAskRepository dailyAskRepository;

    public void aggregate(String date) {
        chatProcessor.process(date);
    }

    public DailyAskSummary getSummary(String date, String startDueDate, String endDueDate, String bondType, String grade) {
        // 주어진 날짜에 생성된 DailyAsk 데이터 조회
        List<DailyAsk> dailyAsks = dailyAskRepository.findByCreatedDate(date);

        if (bondType != null && !bondType.isEmpty()) {
            dailyAsks = dailyAsks.stream()
                    .filter(ask -> bondType.equals(ask.getBond().getBondIssuer().getType().name()))
                    .collect(Collectors.toList());
        }

        if (grade != null && !grade.isEmpty()) {
            dailyAsks = dailyAsks.stream()
                    .filter(ask -> grade.equals(ask.getBond().getBondIssuer().getGrade()))
                    .collect(Collectors.toList());
        }

        if ((startDueDate != null && !startDueDate.isEmpty()) || (endDueDate != null && !endDueDate.isEmpty())) {
            dailyAsks = dailyAsks.stream()
                    .filter(ask -> {
                        String dueDate = ask.getBond().getDueDate(); // Bond의 dueDate를 문자열로 가져옴
                        boolean isAfterStart = startDueDate == null || dueDate.compareTo(startDueDate) >= 0;
                        boolean isBeforeEnd = endDueDate == null || dueDate.compareTo(endDueDate) <= 0;
                        return isAfterStart && isBeforeEnd;
                    })
                    .collect(Collectors.toList());
        }

        // 결과 요약 객체 생성 및 반환
        DailyAskSummary summary = new DailyAskSummary();
        summary.setPublicAsks(convertToAskDto(filterAsksByBondIssuerType(dailyAsks, BondType.PUBLIC)));
        summary.setBankAsks(convertToAskDto(filterAsksByBondIssuerType(dailyAsks, BondType.BANK)));
        summary.setSpecializedCreditAsks(convertToAskDto(filterAsksByBondIssuerType(dailyAsks, BondType.SPECIALIZED_CREDIT)));
        summary.setCompanyAsks(convertToAskDto(filterAsksByBondIssuerType(dailyAsks, BondType.COMPANY)));

        return summary;
    }

    private List<DailyAsk> filterAsksByBondIssuerType(List<DailyAsk> dailyAsks, BondType type) {
        return dailyAsks.stream()
                .filter(dailyAsk -> dailyAsk.getBond().getBondIssuer().getType() == type)
                .collect(Collectors.toList());
    }

    private List<DailyAskDto> convertToAskDto(List<DailyAsk> dailyAsks) {
        return dailyAsks.stream()
                .map(dailyAsk -> DailyAskDto.builder()
                        .bondName(dailyAsk.getBond().getBondIssuer().getName())
                        .dueDate(dailyAsk.getBond().getDueDate())
                        .consecutiveDays(dailyAsk.getConsecutiveDays())
                        .build())
                .collect(Collectors.toList());
    }

}
