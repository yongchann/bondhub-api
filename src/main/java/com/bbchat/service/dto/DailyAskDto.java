package com.bbchat.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor
@Builder
@Getter
public class DailyAskDto {

    private String bondName;

    private String dueDate;

    private int consecutiveDays;
}
