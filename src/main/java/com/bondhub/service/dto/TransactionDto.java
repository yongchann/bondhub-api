package com.bondhub.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
@Builder
@Getter
public class TransactionDto {

    private Long id;

    private String bondName;

    private String dueDate;

    private List<TransactionDetailDto> details;
}
