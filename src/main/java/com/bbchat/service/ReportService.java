package com.bbchat.service;

import com.bbchat.domain.report.DailyReport;
import com.bbchat.repository.DailyReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class ReportService {

    private final DailyReportRepository dailyReportRepository;

    public List<String> findDatesReported(String from, String to) {
        List<DailyReport> reports = dailyReportRepository.findReport(from, to);
        return reports.stream().map(DailyReport::getReportDate).toList();
    }
}
