package com.bbchat.domain.report;

import com.bbchat.domain.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Entity
public class DailyReport extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "daily_aggregation_id")
    private Long id;

    private String reportFileLocation;

    private String reportDate;

    @Enumerated(EnumType.STRING)
    private ReportStatus status;

    private DailyReport(String reportDate, ReportStatus status) {
        this.reportDate = reportDate;
        this.status = status;
    }

    public static DailyReport ready(String reportDate) {
        return new DailyReport(reportDate, ReportStatus.READY);
    }
}
