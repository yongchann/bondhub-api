package com.bbchat.domain.aggregation;

import com.bbchat.domain.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Getter
@Entity
public class DailyAggregation extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "daily_aggregation_id")
    private Long id;

    private boolean hasReported;

    private String reportFileLocation;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_aggregation_id")
    private ChatAggregation chatAggregation;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_aggregation_id")
    private TransactionAggregation transactionAggregation;
}
