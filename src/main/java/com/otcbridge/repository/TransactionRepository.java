package com.otcbridge.repository;

import com.otcbridge.domain.bond.BondType;
import com.otcbridge.domain.transaction.Transaction;
import com.otcbridge.domain.transaction.TransactionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByTransactionDateAndStatus(String transactionDate, TransactionStatus status);

    @Modifying
    @Query("DELETE FROM Transaction t WHERE t.transactionDate = :transactionDate")
    int deleteAllByTransactionDateInBatch(String transactionDate);

    @Query("SELECT t FROM Transaction t " +
            "JOIN FETCH t.bond b " +
            "JOIN FETCH b.bondIssuer bi " +
            "WHERE t.transactionDate = :transactionDate " +
            "AND t.status = 'OK' " +
            "AND bi.type = :bondType " +
            "AND b.dueDate BETWEEN :startDueDate AND :endDueDate " +
            "AND bi.grade IN :grades")
    List<Transaction> findByTransactionDateAndBondTypeAndGrades(
            @Param("transactionDate") String transactionDate,
            @Param("bondType") BondType bondType,
            @Param("startDueDate") String startDueDate,
            @Param("endDueDate") String endDueDate,
            @Param("grades") List<String> grades);

    @Query("SELECT t FROM Transaction t " +
            "JOIN FETCH t.bond b " +
            "JOIN FETCH b.bondIssuer bi " +
            "WHERE t.transactionDate = :transactionDate " +
            "AND t.status = 'OK'")
    List<Transaction> findFetchBondAndBondIssuerByTransactionDate(String transactionDate);

}
