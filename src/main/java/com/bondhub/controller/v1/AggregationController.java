package com.bondhub.controller.v1;

import com.bondhub.controller.v1.response.ChatAggregationResponse;
import com.bondhub.controller.v1.response.TransactionAggregationResponse;
import com.bondhub.domain.aggregation.ChatAggregationResult;
import com.bondhub.domain.aggregation.TransactionAggregationResult;
import com.bondhub.service.AggregationService;
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

    @PostMapping("/api/v1/aggregation/all-chat")
    public void aggregateAllDailyChat(@RequestParam("date") String date) {
        aggregationService.aggregateChat(date);
    }

    @GetMapping("/api/v1/aggregation/chat")
    public ChatAggregationResponse getChatAggregation(@RequestParam("date") String date) {
        ChatAggregationResult result = aggregationService.getChatAggregation(date);
        return ChatAggregationResponse.from(result);
    }

    @GetMapping("/api/v1/aggregation/chat/history")
    public List<ChatAggregationResult> getChatAggregationHistory(@RequestParam("date") String date) {
        return aggregationService.getChatAggregationHistory(date);
    }

    @PostMapping("/api/v1/aggregation/transaction")
    public void aggregateDailyTransaction(@RequestParam("date") String date) {
        aggregationService.aggregateTransaction(date);
    }

    @GetMapping("/api/v1/aggregation/transaction")
    public TransactionAggregationResponse getTransactionAggregation(@RequestParam("date") String date) {
        TransactionAggregationResult result = aggregationService.getTransactionAggregation(date);
        return TransactionAggregationResponse.from(result);
    }

}
