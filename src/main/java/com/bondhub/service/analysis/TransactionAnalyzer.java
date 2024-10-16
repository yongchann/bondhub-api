package com.bondhub.service.analysis;

import com.bondhub.domain.transaction.Transaction;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class TransactionAnalyzer {

    private final BondClassifier bondClassifier;

    public void analyze(Transaction tx) {
        bondClassifier.extractCreditBondIssuer(tx.getBondName()).ifPresentOrElse(
                result -> tx.classified(result.bondIssuer(), result.triggerKeyword()), // 분류 성공
                tx::failedClassified);                                  // 분류 실패
    }
}
