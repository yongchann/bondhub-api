package com.bbchat.repository;

import com.bbchat.domain.bond.Bond;
import com.bbchat.domain.bond.BondType;
import com.bbchat.domain.transaction.DailyTransaction;
import com.bbchat.domain.transaction.TransactionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DailyTransactionRepository extends JpaRepository<DailyTransaction, Long> {

    @Query("SELECT da FROM DailyTransaction da WHERE da.bond = :bond AND da.transactionDate = :transactionDate")
    Optional<DailyTransaction> findByBondAndDate(Bond bond, String transactionDate);

    void deleteAllByTransactionDate(String date);

    @Query("SELECT dt FROM DailyTransaction dt " +
            "JOIN FETCH dt.bond b " +
            "JOIN FETCH b.bondIssuer bi " +
            "WHERE dt.transactionDate = :transactionDate")
    List<DailyTransaction> findByCreatedDate(String transactionDate);

    List<DailyTransaction> findByTransactionDateAndStatus(String transactionDate, TransactionStatus status);

    @Modifying
    @Query("DELETE FROM DailyTransaction dt WHERE dt.transactionDate = :transactionDate")
    int deleteAllByTransactionDateInBatch(String transactionDate);

    @Query("SELECT dt FROM DailyTransaction dt " +
            "JOIN FETCH dt.bond b " +
            "JOIN FETCH b.bondIssuer bi " +
            "WHERE dt.transactionDate = :transactionDate " +
            "AND dt.status = 'OK' " +
            "AND bi.type = :bondType " +
            "AND b.dueDate BETWEEN :startDueDate AND :endDueDate " +
            "AND bi.grade IN :grades")
    List<DailyTransaction> findByTransactionDateAndBondTypeAndGrades(
            @Param("transactionDate") String transactionDate,
            @Param("bondType") BondType bondType,
            @Param("startDueDate") String startDueDate,
            @Param("endDueDate") String endDueDate,
            @Param("grades") List<String> grades);

}
