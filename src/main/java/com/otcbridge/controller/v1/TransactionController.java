package com.otcbridge.controller.v1;

import com.otcbridge.controller.v1.request.UpdateTransactionNotUsedRequest;
import com.otcbridge.service.TransactionService;
import com.otcbridge.service.dto.BondGradeCollisionDto;
import com.otcbridge.service.dto.TransactionDto;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
public class TransactionController {

    private final TransactionService transactionService;

    @GetMapping("/api/v1/transaction/uncategorized")
    public List<TransactionDto> findUncategorizedTransactions(@RequestParam("date") String date) {
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
