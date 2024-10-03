package com.bondhub.service.dto;

import com.bondhub.domain.transaction.TransactionAggregation;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Builder
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TransactionAggregationResult {

    private LocalDateTime aggregatedDateTime;

    private long totalTransactionCount;

    private long excludedTransactionCount;

    private long uncategorizedTransactionCount;

    private long fullyProcessedTransactionCount;

    public static TransactionAggregationResult from(TransactionAggregation entity) {
        return TransactionAggregationResult.builder()
                .aggregatedDateTime(entity.getCreatedDateTime())
                .totalTransactionCount(entity.getTotalTransactionCount())
                .excludedTransactionCount(entity.getExcludedTransactionCount())
                .fullyProcessedTransactionCount(entity.getFullyProcessedTransactionCount())
                .uncategorizedTransactionCount(entity.getUncategorizedTransactionCount())
                .build();
    }
}
