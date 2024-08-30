package com.bbchat.controller.v1;

import com.bbchat.controller.v1.request.InquiryAskRequest;
import com.bbchat.service.AggregationService;
import com.bbchat.service.AskService;
import com.bbchat.service.dto.BondChatDto;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RestController
public class AskController {

    private final AskService askService;
    private final AggregationService aggregationService;

    @PostMapping("/api/v1/ask/daily")
    public List<BondChatDto> findAsk(@RequestParam("date")String date, @RequestBody InquiryAskRequest request) {
        return askService.inquiry(date, request.getMaturityCondition(), request.getGrades());
    }

}
