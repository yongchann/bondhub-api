package com.bondhub.service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
@Getter
public class TransactionGroupByBondNameDto {

    private String bondName;

    private List<Long> ids;
}
