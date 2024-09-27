package com.bondhub.service;

import com.bondhub.domain.bond.BondIssuer;
import com.bondhub.domain.transaction.Transaction;
import com.bondhub.domain.transaction.TransactionStatus;
import com.bondhub.repository.TransactionRepository;
import com.bondhub.service.dto.BondGradeCollisionDto;
import com.bondhub.service.dto.TransactionDto;
import com.bondhub.support.FileInfo;
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

    @Transactional
    public int updateNotUsed(String date, List<Long> targetIds) {
        List<Transaction> transactions = transactionRepository.findByTransactionDateAndStatus(date, TransactionStatus.UNCATEGORIZED);
        return transactions.stream()
                .filter(tx -> targetIds.contains(tx.getId()))
                .peek(Transaction::modifyStatusNotUsed)
                .toList().size();
    }

    public List<BondGradeCollisionDto> findGradeCollisionTransactions(String date) {
        List<Transaction> transactions = transactionRepository.findByTransactionDateAndStatus(date, TransactionStatus.AMBIGUOUS_GRADE);

        return transactions.stream()
                .map(tx -> {
                    BondIssuer bondIssuer = tx.getBond().getBondIssuer();
                    return new BondGradeCollisionDto(
                            bondIssuer.getId(), bondIssuer.getName(), bondIssuer.getGrade(), // 기존 등록된 신용등급
                            tx.getBondName(), tx.getCreditRating()); // 거래 내역에서 추출된 신용등급
                }).collect(Collectors.toSet()).stream().toList();
    }

    public InputStream findTransactionFileContent(String date) {
        FileInfo file = fileRepository.get(buildPath(TRANSACTION_FILE_KEY_PREFIX, date), TRANSACTION_FILE_SAVE_NAME);
        return file.getInputStream();
    }

}
