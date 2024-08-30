package com.bbchat.repository;

import com.bbchat.domain.report.DailyReport;
import com.bbchat.domain.report.ReportStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface DailyReportRepository extends JpaRepository<DailyReport, Long> {

    @Query("SELECT dr FROM DailyReport dr " +
            "WHERE dr.reportDate BETWEEN :from AND :to " +
            "AND dr.reportDate is not null")
    List<DailyReport> findReport(String from, String to);

    Optional<DailyReport> findByReportDateAndStatus(String reportDate, ReportStatus status);
}
