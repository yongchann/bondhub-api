package com.bondhub.domain.transaction;

import com.bondhub.domain.bond.BondType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@RequiredArgsConstructor
@Component
public class TxReader {

    private final TransactionRepository transactionRepository;

    public List<Transaction> getClassifiedTx(String txDate, BondType bondType) {
        return transactionRepository.findClassifiedTxWithBond(txDate, bondType);
    }


}
