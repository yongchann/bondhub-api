package com.otcbridge.controller.v1;

import com.otcbridge.controller.v1.request.InquiryAskRequest;
import com.otcbridge.controller.v1.response.DailyAskListResponse;
import com.otcbridge.domain.ask.Ask;
import com.otcbridge.domain.bond.BondType;
import com.otcbridge.service.AskService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@RestController
public class AskController {

    private final AskService askService;

    @PostMapping("/api/v1/ask/daily")
    public List<Ask> findAsk(@RequestParam("date")String date, @RequestBody InquiryAskRequest request) {
        return askService.inquiry(date, request.getBondType(), request.getMaturityCondition(), request.getGrades());
    }

    @GetMapping("/api/v1/ask/daily-all")
    public DailyAskListResponse getAllAsk(@RequestParam("date") String date) {
        Map<BondType, List<Ask>> result = askService.findAllAsk(date);

        return DailyAskListResponse.builder()
                .publicAsks(result.get(BondType.PUBLIC))
                .bankAsks(result.get(BondType.BANK))
                .specializedCreditAsks(result.get(BondType.SPECIALIZED_CREDIT))
                .companyAsks(result.get(BondType.COMPANY))
                .build();
    }
}
