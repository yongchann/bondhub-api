package com.bbchat.controller.v1;

import com.bbchat.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RestController
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/api/v1/report/submitted")
    public List<String> findDatesReported(@RequestParam("from") String from, @RequestParam("to") String to) {
        return reportService.findDatesReported(from, to);
    }
}
