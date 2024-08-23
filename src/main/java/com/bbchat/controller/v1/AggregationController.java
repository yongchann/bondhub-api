package com.bbchat.controller.v1;

import com.bbchat.service.AggregationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class AggregationController {

    private final AggregationService aggregationService;

    @PostMapping("/api/v1/aggregation/chat")
    public void aggregateDaily(@RequestParam("date") String date) {
        aggregationService.aggregate(date);
    }
}
