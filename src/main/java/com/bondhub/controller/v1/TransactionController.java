package com.bondhub.controller.v1;

import com.bondhub.domain.bond.BondType;
import com.bondhub.domain.transaction.TransactionStatus;
import com.bondhub.service.TransactionService;
import com.bondhub.service.dto.TransactionDetailDto;
import com.bondhub.service.dto.TransactionGroupByBondNameDto;
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

    @GetMapping("/api/v1/transaction/group")
    public List<TransactionGroupByBondNameDto> groupedTransactions(@RequestParam("date") String date, @RequestParam("status") TransactionStatus status) {
        return transactionService.getTransactionsGroupByContent(date, status);
    }

    @PostMapping("/api/v1/transaction/reanalyze-all")
    public void aggregateDailyTransaction(@RequestParam("date") String date) {
        transactionService.reanalyzeAll(date);
    }

    @GetMapping("/api/v1/transaction/recent")
    public List<TransactionDetailDto> findTransactionGroup(@RequestParam("bondType") BondType bondType) {
        return transactionService.inquiryRecent(bondType);
    }
}
