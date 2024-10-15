package com.bondhub.controller.v1;

import com.bondhub.service.TransactionService;
import com.bondhub.service.dto.TransactionDto;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RestController
public class TransactionController {

    private final TransactionService transactionService;

    @GetMapping("/api/v1/transaction/uncategorized")
    public List<TransactionDto> findUncategorizedTransactions(@RequestParam("date") String date) {
        return transactionService.findUncategorized(date);
    }

    @PostMapping("/api/v1/transaction/reanalyze-all")
    public void aggregateDailyTransaction(@RequestParam("date") String date) {
        transactionService.reanalyzeAll(date);
    }
}
