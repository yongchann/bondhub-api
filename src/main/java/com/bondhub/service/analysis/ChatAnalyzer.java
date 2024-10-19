package com.bondhub.service.analysis;

import com.bondhub.domain.bond.NonCreditClassificationResult;
import com.bondhub.domain.chat.Chat;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@RequiredArgsConstructor
@Component
public class ChatAnalyzer {

    private final BondClassifier bondClassifier;

    public void analyze(Chat chat) {
        // 매매 유형 설정
        chat.initializeTradeType();

        Optional<NonCreditClassificationResult> nonCreditBondType = bondClassifier.extractNonCreditBondType(chat.getContent());
        if (nonCreditBondType.isPresent()) {
            chat.classified(nonCreditBondType.get().bondType(), nonCreditBondType.get().triggerKeyword());
            return;
        }

        bondClassifier.extractCreditBondIssuer(chat.getContent()).ifPresentOrElse(
                result -> chat.classified(result.bondIssuer(), result.triggerKeyword()), // 분류 성공
                chat::failedClassified);                                  // 분류 실패

    }
}
