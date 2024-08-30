package com.bbchat.service;

import com.bbchat.domain.bond.Bond;
import com.bbchat.domain.bond.BondIssuer;
import com.bbchat.domain.transaction.DailyTransaction;
import com.bbchat.domain.transaction.TransactionStatus;
import com.bbchat.repository.DailyTransactionRepository;
import com.bbchat.service.dto.BondGradeCollisionDto;
import com.bbchat.service.dto.DailyTransactionDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class DailyTransactionService {

    private final DailyTransactionRepository transactionRepository;

    public List<DailyTransactionDto> findUncategorized(String date) {
        List<DailyTransaction> transactions = transactionRepository.findByTransactionDateAndStatus(date, TransactionStatus.UNCATEGORIZED);
        return transactions.stream()
                .map(tx -> DailyTransactionDto.builder()
                        .id(tx.getId())
                        .bondName(tx.getBondName())
                        .dueDate(tx.getMaturityDate())
                        .build())
                .toList();
    }

    @Transactional
    public int updateNotUsed(String date, List<Long> targetIds) {
        List<DailyTransaction> transactions = transactionRepository.findByTransactionDateAndStatus(date, TransactionStatus.UNCATEGORIZED);
        return transactions.stream()
                .filter(tx -> targetIds.contains(tx.getId()))
                .peek(DailyTransaction::modifyStatusNotUsed)
                .toList().size();
    }

    public List<BondGradeCollisionDto> findGradeCollisionTransactions(String date) {
        List<DailyTransaction> transactions = transactionRepository.findByTransactionDateAndStatus(date, TransactionStatus.AMBIGUOUS_GRADE);

        return transactions.stream()
                .map(tx -> {
                    BondIssuer bondIssuer = tx.getBond().getBondIssuer();
                    return new BondGradeCollisionDto(
                            bondIssuer.getId(), bondIssuer.getName(), bondIssuer.getGrade(), // 기존 등록된 신용등급
                            tx.getBondName(), tx.getCreditRating()); // 거래 내역에서 추출된 신용등급
                }).collect(Collectors.toSet()).stream().toList();
    }

}
