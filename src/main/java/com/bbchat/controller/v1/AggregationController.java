package com.bbchat.controller.v1;

import com.bbchat.service.AskService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class AggregationController {

    private final AskService askService;

    @PostMapping("/api/v1/aggregation/chat")
    public void aggregateDaily(@RequestParam("date") String date) {
        askService.aggregate(date);
    }
}
