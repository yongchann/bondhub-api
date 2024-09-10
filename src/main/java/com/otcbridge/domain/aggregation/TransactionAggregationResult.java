package com.otcbridge.domain.aggregation;

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
                .aggregatedDateTime(entity.getCreatedDate())
                .totalTransactionCount(entity.getTotalTransactionCount())
                .excludedTransactionCount(entity.getExcludedTransactionCount())
                .fullyProcessedTransactionCount(entity.getFullyProcessedTransactionCount())
                .uncategorizedTransactionCount(entity.getUncategorizedTransactionCount())
                .build();
    }
}
