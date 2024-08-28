package com.bbchat.service;

import com.bbchat.domain.aggregation.ChatAggregation;
import com.bbchat.domain.aggregation.ChatAggregationResult;
import com.bbchat.domain.aggregation.TransactionAggregation;
import com.bbchat.domain.aggregation.TransactionAggregationResult;
import com.bbchat.repository.ChatAggregationRepository;
import com.bbchat.repository.TransactionAggregationRepository;
import com.bbchat.support.FileInfo;
import com.bbchat.support.S3FileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@RequiredArgsConstructor
@Service
public class AggregationService {

    private final S3FileRepository fileRepository;
    private final ChatProcessor chatProcessor;
    private final TransactionProcessor transactionProcessor;
    private final ChatAggregationRepository chatAggregationRepository;
    private final TransactionAggregationRepository transactionAggregationRepository;

    public void aggregateChat(String date) {
        FileInfo file = fileRepository.getChatFileByDate(date);

        ChatAggregation aggregation = chatAggregationRepository.findByChatDate(date)
                .orElse(new ChatAggregation(file.getFilename(), date));

        ChatAggregationResult result = chatProcessor.aggregateFromRawContent(date, file.getContent());
        aggregation.update(LocalDateTime.now(), result);
        chatAggregationRepository.save(aggregation);
    }

    public void aggregateTransaction(String date) {
        FileInfo file = fileRepository.getTransactionFileByDate(date);

        TransactionAggregation aggregation = transactionAggregationRepository.findByTransactionDate(date)
                .orElse(new TransactionAggregation(file.getFilename(), date));

        TransactionAggregationResult result = transactionProcessor.aggregateFromInputStream(date, file.getInputStream());
        aggregation.update(LocalDateTime.now(), result);
        transactionAggregationRepository.save(aggregation);
    }

}
