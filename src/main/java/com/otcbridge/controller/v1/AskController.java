package com.otcbridge.controller.v1;

import com.otcbridge.controller.v1.request.InquiryAskRequest;
import com.otcbridge.service.AskService;
import com.otcbridge.service.dto.BondChatDto;
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

    @PostMapping("/api/v1/ask/daily")
    public List<BondChatDto> findAsk(@RequestParam("date")String date, @RequestBody InquiryAskRequest request) {
        return askService.inquiry(date, request.getBondType(), request.getMaturityCondition(), request.getGrades());
    }

}
