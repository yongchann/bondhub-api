package com.bondhub.service;

import com.bondhub.domain.aggregation.AnalysisSummaryUpdater;
import com.bondhub.domain.common.FileInfo;
import com.bondhub.domain.transaction.Transaction;
import com.bondhub.domain.transaction.TransactionFinder;
import com.bondhub.domain.transaction.TransactionMutator;
import com.bondhub.domain.transaction.TransactionStatus;
import com.bondhub.service.analysis.TransactionAnalyzer;
import com.bondhub.service.analysis.TransactionParser;
import com.bondhub.service.dto.TransactionGroupByBondNameDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class TransactionService {

    private final TransactionFinder transactionFinder;
    private final TransactionMutator transactionMutator;

    private final TransactionParser transactionParser;
    private final TransactionAnalyzer transactionAnalyzer;

    private final AnalysisSummaryUpdater analysisSummaryUpdater;

    @Transactional
    public void reanalyzeAll(String date) {
        // 해당 일자의 거래내역을 모두 삭제
        int deletedCount = transactionMutator.deleteAllByTransactionDateInBatch(date);
        log.info("[aggregateTransaction] deleted {} transactions", deletedCount);

        FileInfo file = transactionFinder.getDailyTransactionFile(date);
        InputStream inputStream = file.getInputStream();

        // 집계
        List<Transaction> allTx = transactionParser.processTransactionFileInputStream(date, inputStream);
        allTx.forEach(transactionAnalyzer::analyze);
        transactionMutator.saveAll(allTx);
        log.info("[aggregateTransaction] created {} transactions", allTx.size());

        analysisSummaryUpdater.updateTransactionSummary(date, allTx);
    }

    public List<TransactionGroupByBondNameDto> getTransactionsGroupByContent(String date, TransactionStatus status) {
        List<Transaction> txs = transactionFinder.findDailyByStatus(date, status);

        Map<String, List<Transaction>> groupedByBondName = txs.stream().collect(Collectors.groupingBy(Transaction::getBondName));

        List<TransactionGroupByBondNameDto> result = new ArrayList<>();
        groupedByBondName.forEach((bondName, value) -> {
            List<Long> ids = value.stream().map(Transaction::getId).toList();
            result.add(new TransactionGroupByBondNameDto(bondName, ids));
        });

        result.sort((c1, c2) -> Integer.compare(c2.getIds().size(), c1.getIds().size()));
        return result;
    }

}
