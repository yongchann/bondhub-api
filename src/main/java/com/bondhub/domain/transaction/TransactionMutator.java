package com.bondhub.domain.transaction;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@RequiredArgsConstructor
@Component
public class TransactionMutator {

    private final TransactionRepository transactionRepository;

    public int deleteAllByTransactionDateInBatch(String transactionDate) {
        return transactionRepository.deleteAllByTransactionDateInBatch(transactionDate);
    }

    public void saveAll(List<Transaction> txs) {
        transactionRepository.saveAll(txs);

    }
}
