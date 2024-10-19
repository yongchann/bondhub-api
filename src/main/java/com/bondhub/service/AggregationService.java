package com.bondhub.service;

import com.bondhub.domain.aggregation.AnalysisSummaryReader;
import com.bondhub.domain.aggregation.ChatAggregation;
import com.bondhub.domain.aggregation.TransactionAggregation;
import com.bondhub.service.dto.ChatAggregationResult;
import com.bondhub.service.dto.TransactionAggregationResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class AggregationService {

    private final AnalysisSummaryReader analysisSummaryReader;

    public ChatAggregationResult getChatAggregation(String date) {
        ChatAggregation chatAggregation = analysisSummaryReader.getChatAggregation(date);
        return ChatAggregationResult.from(chatAggregation);
    }

    public TransactionAggregationResult getTransactionAggregation(String date) {
        TransactionAggregation aggregation = analysisSummaryReader.getTransactionAggregation(date);
        return TransactionAggregationResult.from(aggregation);
    }

}
