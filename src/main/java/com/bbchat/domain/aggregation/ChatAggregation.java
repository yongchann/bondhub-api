package com.bbchat.domain.aggregation;

import com.bbchat.domain.report.DailyReport;
import jakarta.persistence.*;
import lombok.*;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Getter
@Entity
public class ChatAggregation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "chat_aggregation_id")
    private Long id;

    private String chatDate;

    private String roomType;

    @Embedded
    private ChatAggregationResult result;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "daily_report_id")
    private DailyReport dailyReport;

}
