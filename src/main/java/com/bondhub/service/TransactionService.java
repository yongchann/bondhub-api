package com.bondhub.service;

import com.bondhub.domain.bond.BondIssuer;
import com.bondhub.domain.transaction.Transaction;
import com.bondhub.domain.transaction.TransactionStatus;
import com.bondhub.domain.transaction.TransactionRepository;
import com.bondhub.service.dto.BondGradeCollisionDto;
import com.bondhub.service.dto.TransactionDto;
import com.bondhub.domain.common.FileInfo;
import com.bondhub.support.S3FileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;

import static com.bondhub.service.UploadService.*;
import static com.bondhub.support.S3FileRepository.buildPath;

@RequiredArgsConstructor
@Service
public class TransactionService {

    private final S3FileRepository fileRepository;
    private final TransactionRepository transactionRepository;

    public List<TransactionDto> findUncategorized(String date) {
        List<Transaction> transactions = transactionRepository.findByTransactionDateAndStatus(date, TransactionStatus.UNCATEGORIZED);
        return transactions.stream()
                .map(tx -> TransactionDto.builder()
                        .id(tx.getId())
                        .bondName(tx.getBondName())
                        .dueDate(tx.getMaturityDate())
                        .build())
                .toList();
    }

    public InputStream findTransactionFileContent(String date) {
        FileInfo file = fileRepository.get(buildPath(TRANSACTION_FILE_KEY_PREFIX, date), TRANSACTION_FILE_SAVE_NAME);
        return file.getInputStream();
    }

}
