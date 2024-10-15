package com.bondhub.service;

import com.bondhub.domain.chat.ChatAggregation;
import com.bondhub.domain.chat.ChatAggregationRepository;
import com.bondhub.domain.transaction.TransactionAggregation;
import com.bondhub.domain.transaction.TransactionAggregationRepository;
import com.bondhub.service.dto.ChatAggregationResult;
import com.bondhub.service.dto.TransactionAggregationResult;
import com.bondhub.service.exception.NotFoundAggregationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class AggregationService {

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
