package com.bbchat.domain.aggregation;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Builder
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TransactionAggregationResult {

    private String fileName;

    private LocalDateTime lastAggregatedDateTime;

    private long totalTransactionCount;

    private long excludedTransactionCount;

    private long ambiguousGradeTransactionCount;

    private long uncategorizedTransactionCount;

    private long fullyProcessedTransactionCount;

    public static TransactionAggregationResult from(TransactionAggregation aggregation) {
        return TransactionAggregationResult.builder()
                .fileName(aggregation.getFileName())
                .lastAggregatedDateTime(aggregation.getLastAggregatedDateTime())
                .totalTransactionCount(aggregation.getTotalTransactionCount())
                .excludedTransactionCount(aggregation.getExcludedTransactionCount())
                .fullyProcessedTransactionCount(aggregation.getFullyProcessedTransactionCount())
                .ambiguousGradeTransactionCount(aggregation.getAmbiguousGradeTransactionCount())
                .uncategorizedTransactionCount(aggregation.getUncategorizedTransactionCount())
                .build();
    }
}
