package com.bondhub.service.dto;

import com.bondhub.domain.transaction.Transaction;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor
@Builder
@Getter
public class TransactionDetailDto {

    private String time;

    private String yield;

    private String tradingYield;

    private String spreadBp;

    public static TransactionDetailDto from(Transaction tx) {
        return TransactionDetailDto.builder()
                .time(tx.getTime())
                .yield(tx.getYield())
                .tradingYield(tx.getTradingYield())
                .spreadBp(tx.getSpreadBp())
                .build();
    }

}
