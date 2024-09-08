package com.bbchat.domain.aggregation;

import com.bbchat.domain.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

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

}
