package com.bbchat.controller.v1.response;

import com.bbchat.domain.aggregation.TransactionAggregationResult;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Builder
@AllArgsConstructor
@Getter
public class TransactionAggregationResponse {

    private String fileName;

    private LocalDateTime lastAggregatedDateTime;

    private long totalTransactionCount;

    private long excludedTransactionCount;

    private long fullyProcessedTransactionCount;

    private long uncategorizedTransactionCount;

    public static TransactionAggregationResponse from(TransactionAggregationResult result) {
        return TransactionAggregationResponse.builder()
                .lastAggregatedDateTime(result.getAggregatedDateTime())
                .totalTransactionCount(result.getTotalTransactionCount())
                .excludedTransactionCount(result.getExcludedTransactionCount())
                .fullyProcessedTransactionCount(result.getFullyProcessedTransactionCount())
                .uncategorizedTransactionCount(result.getUncategorizedTransactionCount())
                .build();
    }

}
