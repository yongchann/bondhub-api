package com.bondhub.domain.transaction;

import com.bondhub.domain.bond.BondType;
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
            "AND bi.type = :bondType")
    List<Transaction> findClassifiedTxWithBond(
            @Param("transactionDate") String txDate,
            @Param("bondType") BondType bondType);

}
