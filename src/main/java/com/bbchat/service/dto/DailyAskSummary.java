package com.bbchat.service.dto;

import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
@Getter
public class DailyAskSummary {

    private List<DailyAskDto> publicAsks;

    private List<DailyAskDto> bankAsks;

    private List<DailyAskDto> specializedCreditAsks;

    private List<DailyAskDto> companyAsks;

}