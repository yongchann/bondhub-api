package com.bbchat.repository;

import com.bbchat.domain.aggregation.DailyReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface DailyReportRepository extends JpaRepository<DailyReport, Long> {

    @Query("SELECT dr FROM DailyReport dr " +
            "WHERE dr.reportDate BETWEEN :from AND :to " +
            "AND dr.reportDate is not null")
    List<DailyReport> findReport(String from, String to);

    Optional<DailyReport> findByReportDate(String reportDate);
}
