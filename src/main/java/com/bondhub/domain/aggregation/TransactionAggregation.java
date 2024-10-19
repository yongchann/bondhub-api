package com.bondhub.domain.aggregation;

import com.bondhub.domain.common.BaseTimeEntity;
import com.bondhub.domain.transaction.TransactionStatus;
import jakarta.persistence.*;
import lombok.*;

import java.util.Map;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Getter
@Entity
public class TransactionAggregation extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "transaction_aggregation_id")
    private Long id;

    private String transactionDate;

    private long totalTransactionCount;

    private long excludedTransactionCount;

    private long uncategorizedTransactionCount;

    private long fullyProcessedTransactionCount;

    public static TransactionAggregation init(String date) {
        return TransactionAggregation.builder()
                .transactionDate(date)
                .build();
    }

    public void update(Map<TransactionStatus, Long> statusCounts) {
        this.totalTransactionCount += statusCounts.values().stream().mapToLong(Long::longValue).sum();
        this.uncategorizedTransactionCount += statusCounts.getOrDefault(TransactionStatus.UNCATEGORIZED, 0L);
        this.fullyProcessedTransactionCount += statusCounts.getOrDefault(TransactionStatus.OK, 0L);
    }
}
