package com.bbchat.service;

import com.bbchat.domain.dto.AskDto;
import com.bbchat.domain.BondType;
import com.bbchat.domain.DailyAskSummary;
import com.bbchat.domain.dto.MultiDueDateChatDto;
import com.bbchat.domain.entity.DailyAsk;
import com.bbchat.domain.entity.MultiDueDateChat;
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
    private final MultiDueDateChatRepository multiDueDateChatRepository;

    public void aggregate(String date) {
        chatProcessor.process(date);
    }

    public DailyAskSummary getSummary(String date) {
        List<DailyAsk> dailyAsks = dailyAskRepository.findByCreatedDate(date);

        // 필터링 및 그룹화를 위해 BondIssuer를 기준으로 분류합니다.
        List<DailyAsk> publicAsks = filterAsksByBondIssuerType(dailyAsks, BondType.PUBLIC);
        List<DailyAsk> bankAsks = filterAsksByBondIssuerType(dailyAsks, BondType.BANK);
        List<DailyAsk> specializedCreditAsks = filterAsksByBondIssuerType(dailyAsks, BondType.SPECIALIZED_CREDIT);
        List<DailyAsk> companyAsks = filterAsksByBondIssuerType(dailyAsks, BondType.COMPANY);
        List<MultiDueDateChat> multiDueDateChats = multiDueDateChatRepository.findByChatCreatedDate(date);

        DailyAskSummary summary = new DailyAskSummary();
        summary.setPublicAsks(convertToAskDto(publicAsks));
        summary.setBankAsks(convertToAskDto(bankAsks));
        summary.setSpecializedCreditAsks(convertToAskDto(specializedCreditAsks));
        summary.setCompanyAsks(convertToAskDto(companyAsks));
        summary.setMultiDueDateChats(convertToMultiDueDateChatDto(multiDueDateChats));

        return summary;
    }

    private List<DailyAsk> filterAsksByBondIssuerType(List<DailyAsk> dailyAsks, BondType type) {
        return dailyAsks.stream()
                .filter(dailyAsk -> dailyAsk.getBond().getBondIssuer().getType() == type)
                .collect(Collectors.toList());
    }

    private List<AskDto> convertToAskDto(List<DailyAsk> dailyAsks) {
        return dailyAsks.stream()
                .map(dailyAsk -> AskDto.builder()
                        .bondName(dailyAsk.getBond().getBondIssuer().getName())
                        .dueDate(dailyAsk.getBond().getDueDate())
                        .consecutiveDays(dailyAsk.getConsecutiveDays())
                        .build())
                .collect(Collectors.toList());
    }

    private List<MultiDueDateChatDto> convertToMultiDueDateChatDto(List<MultiDueDateChat> multiDueDateChats) {
        return multiDueDateChats.stream()
                .map(chat -> MultiDueDateChatDto.builder()
                        .content(chat.getContent())
                        .id(chat.getId())
                        .build())
                .toList();
    }

}
