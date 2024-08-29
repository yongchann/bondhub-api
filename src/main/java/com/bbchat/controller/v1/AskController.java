package com.bbchat.controller.v1;

import com.bbchat.controller.v1.request.InquiryAskRequest;
import com.bbchat.controller.v1.response.InquiryAskResponse;
import com.bbchat.domain.aggregation.ChatAggregationResult;
import com.bbchat.domain.bond.Bond;
import com.bbchat.service.AggregationService;
import com.bbchat.service.AskService;
import com.bbchat.service.dto.DailyAskDto;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Map;

@RequiredArgsConstructor
@RestController
public class AskController {

    private final AskService askService;
    private final AggregationService aggregationService;

    @PostMapping("/api/v1/ask/daily")
    public InquiryAskResponse findAsk(@RequestParam("date")String date, @RequestBody InquiryAskRequest request) {
        Map<Bond, DailyAskDto> result = askService.inquiry(date, request.getMaturityCondition(), request.getGrades());
        ChatAggregationResult aggregation = aggregationService.getChatAggregation(date);

        return InquiryAskResponse.builder()
                .asks(new ArrayList<>(result.values()))
                .fullyProcessedChatCount(aggregation.getFullyProcessedChatCount())
                .lastAggregatedDateTime(aggregation.getLastAggregatedDateTime())
                .totalChatCount(aggregation.getTotalChatCount())
                .fileName(aggregation.getFileName())
                .build();
    }

}
