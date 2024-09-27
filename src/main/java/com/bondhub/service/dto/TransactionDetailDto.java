package com.bondhub.service.dto;

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

}
