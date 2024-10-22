package com.bondhub.domain.transaction;

import com.bondhub.service.dto.TransactionDetailDto;
import com.bondhub.service.dto.TransactionGroupDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Component
public class TransactionProcessor {

    public List<TransactionGroupDto> groupByBond(List<Transaction> transactions) {
        return transactions.stream()
                .collect(Collectors.groupingBy(
                        Transaction::getBond,
                        Collectors.mapping(
                                TransactionDetailDto::from,
                                Collectors.toList()
                        )
                ))
                .entrySet()
                .stream()
                .map(entry -> TransactionGroupDto.builder()
                        .bond(entry.getKey())
                        .details(entry.getValue())
                        .build())
                .collect(Collectors.toList());
    }
}
