package com.bbchat.controller.v1;

import com.bbchat.controller.v1.request.UpdateTransactionNotUsedRequest;
import com.bbchat.service.DailyTransactionService;
import com.bbchat.service.dto.BondGradeCollisionDto;
import com.bbchat.service.dto.DailyTransactionDto;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
public class TransactionController {

    private final DailyTransactionService transactionService;

    @GetMapping("/api/v1/transaction/uncategorized")
    public List<DailyTransactionDto> findUncategorizedTransactions(@RequestParam("date") String date) {
        return transactionService.findUncategorized(date);
    }

    @PatchMapping("/api/v1/transaction/not-used")
    public int updateNotUsed(@RequestParam("date") String date, @RequestBody UpdateTransactionNotUsedRequest request) {
        return transactionService.updateNotUsed(date, request.getTransactionIds());
    }

    @GetMapping("/api/v1/transaction/grade-collision")
    public List<BondGradeCollisionDto> findGradeCollisionTransactions(@RequestParam("date") String date) {
        return transactionService.findGradeCollisionTransactions(date);
    }
}
