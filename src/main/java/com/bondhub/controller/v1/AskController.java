package com.bondhub.controller.v1;

import com.bondhub.domain.ask.Ask;
import com.bondhub.domain.bond.BondType;
import com.bondhub.service.AskService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
public class AskController {

    private final AskService askService;

    @GetMapping("/api/v1/ask/daily")
    public List<Ask> findAsk(@RequestParam("date")String date, @RequestParam("bondType") BondType bondType) {
        return askService.inquiry(date, bondType);
    }
}
