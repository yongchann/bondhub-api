package com.bondhub.service;

import com.bondhub.domain.common.FileInfo;
import com.bondhub.domain.transaction.*;
import com.bondhub.service.analysis.TransactionAnalyzer;
import com.bondhub.service.analysis.TransactionParser;
import com.bondhub.service.dto.TransactionDto;
import com.bondhub.support.S3FileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.bondhub.service.UploadService.TRANSACTION_FILE_KEY_PREFIX;
import static com.bondhub.service.UploadService.TRANSACTION_FILE_SAVE_NAME;
import static com.bondhub.support.S3FileRepository.buildPath;

@Slf4j
@RequiredArgsConstructor
@Service
public class TransactionService {

    private final TransactionParser transactionParser;
    private final TransactionAnalyzer transactionAnalyzer;

    private final S3FileRepository fileRepository;
    private final TransactionRepository transactionRepository;
    private final TransactionAggregationRepository transactionAggregationRepository;

    public List<TransactionDto> findUncategorized(String date) {
        List<Transaction> transactions = transactionRepository.findByTransactionDateAndStatus(date, TransactionStatus.UNCATEGORIZED);
        return transactions.stream()
                .map(tx -> TransactionDto.builder()
                        .id(tx.getId())
                        .bondName(tx.getBondName())
                        .maturityDate(tx.getMaturityDate())
                        .build())
                .toList();
    }

    @Transactional
    public void reanalyzeAll(String date) {
        // 해당 일자의 거래내역을 모두 삭제
        int deletedCount = transactionRepository.deleteAllByTransactionDateInBatch(date);
        log.info("[aggregateTransaction] deleted {} transactions", deletedCount);

        FileInfo file = fileRepository.get(buildPath(TRANSACTION_FILE_KEY_PREFIX, date), TRANSACTION_FILE_SAVE_NAME);
        InputStream inputStream = file.getInputStream();

        // 집계
        List<Transaction> allTx = transactionParser.processTransactionFileInputStream(date, inputStream);
        allTx.forEach(transactionAnalyzer::analyze);
        transactionRepository.saveAll(allTx);
        log.info("[aggregateTransaction] created {} transactions", allTx.size());

        Map<TransactionStatus, Long> statusCounts = allTx.stream()
                .collect(Collectors.groupingBy(Transaction::getStatus, Collectors.counting()));

        // 집계 결과 생성
        TransactionAggregation aggregation = TransactionAggregation.builder()
                .transactionDate(date)
                .totalTransactionCount(allTx.size())
                .excludedTransactionCount(statusCounts.getOrDefault(TransactionStatus.NOT_USED, 0L))
                .uncategorizedTransactionCount(statusCounts.getOrDefault(TransactionStatus.UNCATEGORIZED, 0L))
                .fullyProcessedTransactionCount(statusCounts.getOrDefault(TransactionStatus.OK, 0L))
                .build();

        transactionAggregationRepository.save(aggregation);
    }

}
