package com.bondhub.controller.v1;

import com.bondhub.controller.v1.request.InquiryAskRequest;
import com.bondhub.domain.ask.Ask;
import com.bondhub.service.AskService;
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
    public List<Ask> findAsk(@RequestParam("date")String date, @RequestBody InquiryAskRequest request) {
        return askService.inquiry(date, request.getBondType(), request.getMaturityCondition(), request.getGrades());
    }
}
