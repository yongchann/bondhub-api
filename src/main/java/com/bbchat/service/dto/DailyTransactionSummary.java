package com.bbchat.service.dto;

import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
@Getter
public class DailyTransactionSummary {

    private List<DailyTransactionDto> publicTransactions;

    private List<DailyTransactionDto> bankTransactions;

    private List<DailyTransactionDto> specializedCreditTransactions;

    private List<DailyTransactionDto> companyTransactions;
}
