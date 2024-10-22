package com.bondhub.controller.v1;

import com.bondhub.domain.ask.SimpleAsk;
import com.bondhub.domain.bond.BondType;
import com.bondhub.service.AskService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RestController
public class AskController {

    private final AskService askService;

    @GetMapping("/api/v1/ask/daily")
    public List<SimpleAsk> findAsk(@RequestParam("date")String date, @RequestParam("bondType") BondType bondType) {
        return askService.inquiry(date, bondType);
    }
}
