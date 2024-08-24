package com.bbchat.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor
@Builder
@Getter
public class AskDto {

    private String bondName;

    private String dueDate;

    private int consecutiveDays;
}
