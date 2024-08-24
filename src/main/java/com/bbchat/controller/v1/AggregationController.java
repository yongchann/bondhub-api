package com.bbchat.controller.v1;

import com.bbchat.service.dto.DailyAskSummary;
import com.bbchat.service.dto.MultiDueDateChatDto;
import com.bbchat.service.AggregationService;
import com.bbchat.service.MultiDueDateChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RestController
public class AggregationController {

    private final AggregationService aggregationService;
    private final MultiDueDateChatService multiDueDateChatService;

    @PostMapping("/api/v1/aggregation/chat")
    public void aggregateDaily(@RequestParam("date") String date) {
        aggregationService.aggregate(date);
    }

    @GetMapping("/api/v1/aggregation/chat")
    public DailyAskSummary getAggregation(
            @RequestParam("date") String date,
            @RequestParam(value = "startDueDate", required = false) String startDueDate,
            @RequestParam(value = "endDueDate", required = false) String endDueDate,
            @RequestParam(value = "bondType", required = false) String bondType,
            @RequestParam(value = "grade", required = false) String grade) {

        DailyAskSummary summary = aggregationService.getSummary(date, startDueDate, endDueDate, bondType, grade);
        return summary;
    }

    @GetMapping("/api/v1/aggregation/multi-duedate-chat")
    public List<MultiDueDateChatDto> getMultiDueDateChats(@RequestParam("date") String date) {
        return multiDueDateChatService.getByDate(date);
    }

}
