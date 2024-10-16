package com.bondhub.service.analysis;

import com.bondhub.domain.bond.BondClassificationResult;
import com.bondhub.domain.transaction.Transaction;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class TransactionAnalyzer {

    private final BondClassifier bondClassifier;

    public void analyze(Transaction tx) {
        BondClassificationResult result = bondClassifier.extractBondIssuer(tx.getBondName());
        tx.classified(result.bondIssuer(), result.triggerKeyword());
    }
}
