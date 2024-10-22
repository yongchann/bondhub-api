package com.bondhub.service.dto;

import com.bondhub.domain.ask.Bond;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
@Builder
@Getter
public class TransactionGroupDto {

    private Bond bond;

    private List<TransactionDetailDto> details;
}
