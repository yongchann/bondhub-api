package com.bondhub.service.analysis;

import com.bondhub.domain.chat.Chat;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@RequiredArgsConstructor
@Component
public class ChatAnalyzer {

    private final MaturityDateExtractor maturityDateExtractor;
    private final BondClassifier bondClassifier;

    public void analyze(Chat chat) {
        // 매매 유형 설정
        chat.initializeTradeType();

        // 만기 설정
        List<String> maturities = maturityDateExtractor.extractAllMaturities(chat.getContent());
        chat.setMaturityDate(maturities);

        if (chat.getMaturityDateCount() == 0) {
            /**
             * TODO 만기가 추출되지 않은 채팅 처리
             * 1. 쓰레기
             * 2. 국고채
             * 3. 매수 호가
             * 4. 만기 중 연도가 생략된 단기 채권
             */
        } else if (chat.getMaturityDateCount() == 1) {
            bondClassifier.extractCreditBondIssuer(chat.getContent()).ifPresentOrElse(
                    result -> chat.classified(result.bondIssuer(), result.triggerKeyword()), // 분류 성공
                    chat::failedClassified);                                  // 분류 실패

        } else {
            // TODO 여러 만기가 추출된 채팅 핸들링
        }


    }


}
