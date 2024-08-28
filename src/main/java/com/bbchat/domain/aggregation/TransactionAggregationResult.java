package com.bbchat.domain.aggregation;

import lombok.*;

@Getter
@Builder
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TransactionAggregationResult {

    private long totalTransactionCount;

    private long excludedTransactionCount;

    private long ambiguousGradeTransactionCount;

    private long uncategorizedTransactionCount;

    private long fullyProcessedTransactionCount;
}
