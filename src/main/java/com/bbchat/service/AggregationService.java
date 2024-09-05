package com.bbchat.service;

import com.bbchat.domain.aggregation.ChatAggregation;
import com.bbchat.domain.aggregation.ChatAggregationResult;
import com.bbchat.domain.aggregation.TransactionAggregation;
import com.bbchat.domain.aggregation.TransactionAggregationResult;
import com.bbchat.repository.ChatAggregationRepository;
import com.bbchat.repository.TransactionAggregationRepository;
import com.bbchat.service.exception.NotFoundAggregationException;
import com.bbchat.support.FileInfo;
import com.bbchat.support.S3FileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.bbchat.service.UploadService.*;
import static com.bbchat.support.S3FileRepository.buildPath;

@RequiredArgsConstructor
@Service
public class AggregationService {

    private final S3FileRepository fileRepository;
    private final ChatProcessor chatProcessor;
    private final ChatAggregationRepository chatAggregationRepository;

    private final TransactionProcessor transactionProcessor;
    private final TransactionAggregationRepository transactionAggregationRepository;

    @Transactional
    public void aggregateChat(String date, String roomType) {
        String filePath = buildPath(CHAT_FILE_KEY_PREFIX, date, roomType);
        FileInfo file = fileRepository.get(filePath, CHAT_FILE_SAVE_NAME);

        // 집계
        ChatAggregationResult result = chatProcessor.aggregateFromRawContent(date, file.getContent(), roomType);

        // 집계 결과를 토대로 ChatAggregation 생성
        ChatAggregation aggregation = ChatAggregation.builder()
                .chatDate(date)
                .roomType(roomType)
                .result(result)
                .build();

        chatAggregationRepository.save(aggregation);
    }

    @Transactional
    public void aggregateTransaction(String date) {
        String filePath = buildPath(TRANSACTION_FILE_KEY_PREFIX, date);
        FileInfo file = fileRepository.get(filePath, TRANSACTION_FILE_SAVE_NAME);

        // 집계
        TransactionAggregationResult result = transactionProcessor.aggregateFromInputStream(date, file.getInputStream());

        // 집계 결과를 토대로 TransactionAggregation 생성
        TransactionAggregation aggregation = TransactionAggregation.builder()
                .transactionDate(date)
                .result(result)
                .build();

        transactionAggregationRepository.save(aggregation);
    }

    public ChatAggregationResult getChatAggregation(String date,String roomType) {
        ChatAggregation aggregation = chatAggregationRepository.findTopByChatDateAndRoomTypeOrderByResultAggregatedDateTimeDesc(date, roomType)
                .orElseThrow(() -> new NotFoundAggregationException("not found chat aggregation of " + date));

        return ChatAggregationResult.from(aggregation);
    }

    public TransactionAggregationResult getTransactionAggregation(String date) {
        TransactionAggregation aggregation = transactionAggregationRepository.findByTransactionDate(date)
                .orElseThrow(() -> new NotFoundAggregationException("not found chat aggregation of " + date));

        return TransactionAggregationResult.from(aggregation);
    }

}
