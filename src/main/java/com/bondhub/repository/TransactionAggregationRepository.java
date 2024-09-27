package com.bondhub.repository;

import com.bondhub.domain.aggregation.TransactionAggregation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TransactionAggregationRepository extends JpaRepository<TransactionAggregation, Long> {

    Optional<TransactionAggregation> findByTransactionDate(String transactionDate);

    Optional<TransactionAggregation> findTopByTransactionDateOrderByCreatedDateDesc(String transactionDate);
}
