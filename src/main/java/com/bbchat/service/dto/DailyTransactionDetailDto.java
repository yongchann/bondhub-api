package com.bbchat.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor
@Builder
@Getter
public class DailyTransactionDetailDto {

    private String yield;

    private String tradingYield;

    private String spreadBp;

}
