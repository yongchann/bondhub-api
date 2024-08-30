package com.bbchat.domain.aggregation;

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

    private long ambiguousGradeTransactionCount;

    private long uncategorizedTransactionCount;

    private long fullyProcessedTransactionCount;

    public static TransactionAggregationResult from(TransactionAggregation entity) {
        return TransactionAggregationResult.builder()
                .aggregatedDateTime(entity.getResult().getAggregatedDateTime())
                .totalTransactionCount(entity.getResult().getTotalTransactionCount())
                .excludedTransactionCount(entity.getResult().getExcludedTransactionCount())
                .fullyProcessedTransactionCount(entity.getResult().getFullyProcessedTransactionCount())
                .ambiguousGradeTransactionCount(entity.getResult().getAmbiguousGradeTransactionCount())
                .uncategorizedTransactionCount(entity.getResult().getUncategorizedTransactionCount())
                .build();
    }
}
