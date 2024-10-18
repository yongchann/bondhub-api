package com.bondhub.domain.aggregation;

import com.bondhub.service.dto.ChatAggregationResult;
import com.bondhub.service.dto.TransactionAggregationResult;
import com.bondhub.service.exception.NotFoundAggregationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class AnalysisSummaryReader {

    private final ChatAggregationRepository chatAggregationRepository;
    private final TransactionAggregationRepository transactionAggregationRepository;

    public ChatAggregationResult getChatAggregation(String date) {
        ChatAggregation aggregation = chatAggregationRepository.findByChatDate(date)
                .orElseThrow(() -> new NotFoundAggregationException("not found chat aggregation of " + date));

        return ChatAggregationResult.from(aggregation);
    }

    public TransactionAggregationResult getTransactionAggregation(String date) {
        TransactionAggregation aggregation = transactionAggregationRepository.findTopByTransactionDateOrderByCreatedDateTimeDesc(date)
                .orElseThrow(() -> new NotFoundAggregationException("not found chat aggregation of " + date));

        return TransactionAggregationResult.from(aggregation);
    }
}
