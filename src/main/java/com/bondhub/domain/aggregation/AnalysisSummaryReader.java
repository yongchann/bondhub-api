package com.bondhub.domain.aggregation;

import com.bondhub.service.exception.NotFoundAggregationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class AnalysisSummaryReader {

    private final ChatAggregationRepository chatAggregationRepository;
    private final TransactionAggregationRepository transactionAggregationRepository;

    public ChatAggregation findOrCreateChatAggregation(String chatDate) {
        return chatAggregationRepository.findByChatDateWithPessimisticLock(chatDate)
                .orElseGet(() -> chatAggregationRepository.save(ChatAggregation.create(chatDate)));
    }

    public TransactionAggregation findOrCreateTransactionAggregation(String date) {
        return transactionAggregationRepository.findTopByTransactionDateOrderByCreatedDateTimeDesc(date)
                .orElseGet(() -> transactionAggregationRepository.save(TransactionAggregation.init(date)));
    }

    public ChatAggregation getChatAggregation(String date) {
        return chatAggregationRepository.findByChatDate(date)
                .orElseThrow(() -> new NotFoundAggregationException("not found chat aggregation of " + date));
    }

    public TransactionAggregation getTransactionAggregation(String date) {
        return transactionAggregationRepository.findTopByTransactionDateOrderByCreatedDateTimeDesc(date)
                .orElseThrow(() -> new NotFoundAggregationException("not found chat aggregation of " + date));
    }
}
