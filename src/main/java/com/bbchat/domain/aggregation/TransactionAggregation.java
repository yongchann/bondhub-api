package com.bbchat.domain.aggregation;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Getter
@Entity
public class TransactionAggregation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "transaction_aggregation_id")
    private Long id;

    private String fileName;

    private String transactionDate;

    private LocalDateTime lastAggregatedDateTime;

    private long totalTransactionCount;

    private long ambiguousGradeTransactionCount;

    private long excludedTransactionCount;

    private long uncategorizedTransactionCount;

    private long fullyProcessedTransactionCount;

    public TransactionAggregation(String fileName, String transactionDate) {
        this.fileName = fileName;
        this.transactionDate = transactionDate;
    }

    public void update(LocalDateTime updateDateTime, TransactionAggregationResult result) {
        this.lastAggregatedDateTime = updateDateTime;
        this.totalTransactionCount = result.getTotalTransactionCount();
        this.ambiguousGradeTransactionCount = result.getAmbiguousGradeTransactionCount();
        this.excludedTransactionCount = result.getExcludedTransactionCount();
        this.uncategorizedTransactionCount = result.getUncategorizedTransactionCount();
        this.fullyProcessedTransactionCount = result.getFullyProcessedTransactionCount();
    }
}
