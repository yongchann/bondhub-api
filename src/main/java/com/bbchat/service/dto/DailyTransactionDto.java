package com.bbchat.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
@Builder
@Getter
public class DailyTransactionDto {

    private Long id;

    private String bondName;

    private String dueDate;

    private List<DailyTransactionDetailDto> details;
}
