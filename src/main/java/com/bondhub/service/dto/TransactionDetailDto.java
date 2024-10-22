package com.bondhub.service.dto;

import com.bondhub.domain.transaction.Transaction;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@AllArgsConstructor
@Builder
@Getter
public class TransactionDetailDto {

    private Long id;

    private String originalBondName;

    private String grade;

    private String maturityDate;

    private LocalDateTime transactionDateTime;

    private String yield;

    private String tradingYield;

    private String spreadBp;

    public static TransactionDetailDto from(Transaction tx) {
        return TransactionDetailDto.builder()
                .id(tx.getId())
                .originalBondName(tx.getBondName())
                .grade(tx.getCreditRating())
                .maturityDate(tx.getMaturityDate())
                .transactionDateTime(tx.getTransactionDateTime())
                .yield(tx.getYield())
                .tradingYield(tx.getTradingYield())
                .spreadBp(tx.getSpreadBp())
                .build();
    }

}
