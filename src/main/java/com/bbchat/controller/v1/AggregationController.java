package com.bbchat.controller.v1;

import com.bbchat.service.dto.DailyAskSummary;
import com.bbchat.service.dto.DailyTransactionSummary;
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
    public void aggregateDailyChat(@RequestParam("date") String date) {
        aggregationService.aggregateChat(date);
    }

    @GetMapping("/api/v1/aggregation/chat")
    public DailyAskSummary getChatAggregation(
            @RequestParam("date") String date,
            @RequestParam(value = "startDueDate", required = false) String startDueDate,
            @RequestParam(value = "endDueDate", required = false) String endDueDate,
            @RequestParam(value = "bondType", required = false) String bondType,
            @RequestParam(value = "grade", required = false) String grade) {

        DailyAskSummary summary = aggregationService.getAskSummary(date, startDueDate, endDueDate, bondType, grade);
        return summary;
    }

    @GetMapping("/api/v1/aggregation/multi-duedate-chat")
    public List<MultiDueDateChatDto> getMultiDueDateChats(@RequestParam("date") String date) {
        return multiDueDateChatService.getByDate(date);
    }

    @PostMapping("/api/v1/aggregation/transaction")
    public void aggregateDailyTransaction(@RequestParam("date") String date) {
        aggregationService.aggregateTransaction(date);
    }

    @GetMapping("/api/v1/aggregation/transaction")
    public DailyTransactionSummary getTransactionAggregation(
            @RequestParam("date") String date,
            @RequestParam(value = "startDueDate", required = false) String startDueDate,
            @RequestParam(value = "endDueDate", required = false) String endDueDate,
            @RequestParam(value = "bondType", required = false) String bondType,
            @RequestParam(value = "grade", required = false) String grade) {

        DailyTransactionSummary summary = aggregationService.getTransactionSummary(date, startDueDate, endDueDate, bondType, grade);
        return summary;
    }


}
