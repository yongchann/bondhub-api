package com.bondhub.controller.v1;

import com.bondhub.controller.v1.request.UpdateTransactionNotUsedRequest;
import com.bondhub.service.TransactionService;
import com.bondhub.service.dto.BondGradeCollisionDto;
import com.bondhub.service.dto.TransactionDto;
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
}
