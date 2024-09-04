package com.bbchat.domain.aggregation;

import jakarta.persistence.*;
import lombok.*;

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

    private String transactionDate;

    @Embedded
    private TransactionAggregationResult result;

}
