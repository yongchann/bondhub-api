package com.bbchat.service;

import com.bbchat.domain.BondType;
import com.bbchat.domain.entity.DailyAsk;
import com.bbchat.domain.entity.DailyTransaction;
import com.bbchat.repository.DailyAskRepository;
import com.bbchat.repository.DailyTransactionRepository;
import com.bbchat.service.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class AggregationService {

    private final ChatProcessor chatProcessor;
    private final DailyAskRepository dailyAskRepository;
    private final DailyTransactionRepository dailyTransactionRepository;

    public void aggregateChat(String date) {
        chatProcessor.processChat(date);
    }

    public void aggregateTransaction(String date) {
        chatProcessor.processTransaction(date);
    }

    public DailyAskSummary getAskSummary(String date, String startDueDate, String endDueDate, String bondType, String grade) {
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

    public DailyTransactionSummary getTransactionSummary(String date, String startDueDate, String endDueDate, String bondType, String grade) {
        List<DailyTransaction> dailyTransactions = dailyTransactionRepository.findByCreatedDate(date);

        if (bondType != null && !bondType.isEmpty()) {
            dailyTransactions = dailyTransactions.stream()
                    .filter(tx -> bondType.equals(tx.getBond().getBondIssuer().getType().name()))
                    .collect(Collectors.toList());
        }

        if (grade != null && !grade.isEmpty()) {
            dailyTransactions = dailyTransactions.stream()
                    .filter(tx -> grade.equals(tx.getBond().getBondIssuer().getGrade()))
                    .collect(Collectors.toList());
        }

        if ((startDueDate != null && !startDueDate.isEmpty()) || (endDueDate != null && !endDueDate.isEmpty())) {
            dailyTransactions = dailyTransactions.stream()
                    .filter(tx -> {
                        String dueDate = tx.getBond().getDueDate();
                        boolean isAfterStart = startDueDate == null || dueDate.compareTo(startDueDate) >= 0;
                        boolean isBeforeEnd = endDueDate == null || dueDate.compareTo(endDueDate) <= 0;
                        return isAfterStart && isBeforeEnd;
                    })
                    .collect(Collectors.toList());
        }

        Map<Long, List<DailyTransaction>> groupedByBond = dailyTransactions.stream()
                .collect(Collectors.groupingBy(transaction -> transaction.getBond().getId()));



        // 4. Map grouped transactions to DTOs
        List<DailyTransactionDto> publicTransactions = mapGroupedToDto(groupedByBond, "PUBLIC");
        List<DailyTransactionDto> bankTransactions = mapGroupedToDto(groupedByBond, "BANK");
        List<DailyTransactionDto> specializedCreditTransactions = mapGroupedToDto(groupedByBond, "SPECIALIZED_CREDIT");
        List<DailyTransactionDto> companyTransactions = mapGroupedToDto(groupedByBond, "COMPANY");

        // 5. Create and return the summary DTO
        return DailyTransactionSummary.builder()
                .publicTransactions(publicTransactions)
                .bankTransactions(bankTransactions)
                .specializedCreditTransactions(specializedCreditTransactions)
                .companyTransactions(companyTransactions)
                .build();
    }


    private List<DailyTransactionDto> mapGroupedToDto(Map<Long, List<DailyTransaction>> groupedByBond, String type) {
        return groupedByBond.entrySet().stream()
                .map(entry -> {
                    List<DailyTransaction> transactions = entry.getValue();
                    // Filter by bond issuer type
                    if (!transactions.get(0).getBond().getBondIssuer().getType().name().equals(type)) {
                        return null;
                    }
                    // Create details
                    List<DailyTransactionDetailDto> details = transactions.stream()
                            .map(transaction -> DailyTransactionDetailDto.builder()
                                    .yield(transaction.getYield())
                                    .tradingYield(transaction.getTradingYield())
                                    .spreadBp(transaction.getSpreadBp())
                                    .build())
                            .collect(Collectors.toList());

                    // Create the DTO
                    DailyTransaction firstTransaction = transactions.get(0);
                    return DailyTransactionDto.builder()
                            .bondName(firstTransaction.getBond().getBondIssuer().getName())
                            .dueDate(firstTransaction.getBond().getDueDate())
                            .consecutiveDays(firstTransaction.getConsecutiveDays())
                            .details(details)
                            .build();
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
}
