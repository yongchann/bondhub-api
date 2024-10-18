package com.bondhub.service;

import com.bondhub.domain.aggregation.*;
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
        return analysisSummaryReader.getChatAggregation(date);
    }

    public TransactionAggregationResult getTransactionAggregation(String date) {
        return analysisSummaryReader.getTransactionAggregation(date);
    }

}
