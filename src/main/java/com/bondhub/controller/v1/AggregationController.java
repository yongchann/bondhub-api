package com.bondhub.controller.v1;

import com.bondhub.controller.v1.response.ChatAggregationResponse;
import com.bondhub.controller.v1.response.TransactionAggregationResponse;
import com.bondhub.service.AggregationService;
import com.bondhub.service.dto.ChatAggregationResult;
import com.bondhub.service.dto.TransactionAggregationResult;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class AggregationController {

    private final AggregationService aggregationService;

    @GetMapping("/api/v1/aggregation/chat")
    public ChatAggregationResponse getChatAggregation(@RequestParam("date") String date) {
        ChatAggregationResult result = aggregationService.getChatAggregation(date);
        return ChatAggregationResponse.from(result);
    }

    @GetMapping("/api/v1/aggregation/transaction")
    public TransactionAggregationResponse getTransactionAggregation(@RequestParam("date") String date) {
        TransactionAggregationResult result = aggregationService.getTransactionAggregation(date);
        return TransactionAggregationResponse.from(result);
    }

}
