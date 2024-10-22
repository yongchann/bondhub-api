package com.bondhub.domain.transaction;

import com.bondhub.domain.bond.BondType;
import com.bondhub.domain.common.FileInfo;
import com.bondhub.support.S3FileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.bondhub.service.UploadService.TRANSACTION_FILE_KEY_PREFIX;
import static com.bondhub.service.UploadService.TRANSACTION_FILE_SAVE_NAME;
import static com.bondhub.support.S3FileRepository.buildPath;

@RequiredArgsConstructor
@Component
public class TransactionFinder {

    private final S3FileRepository fileRepository;
    private final TransactionRepository transactionRepository;

    public List<Transaction> findDailyCreditTransactions(String txDate, BondType bondType) {
        return transactionRepository.findClassifiedTxWithBond(txDate, bondType);
    }

    public List<Transaction> findDailyByStatus(String date, TransactionStatus status) {
        return transactionRepository.findByTransactionDateAndStatus(date, status);
    }

    public FileInfo getDailyTransactionFile(String date) {
        return fileRepository.get(buildPath(TRANSACTION_FILE_KEY_PREFIX, date), TRANSACTION_FILE_SAVE_NAME);

    }

    public List<Transaction> findRecentByBondType(BondType bondType) {
        return transactionRepository.findLatest100ByBondType(bondType);
    }
}
