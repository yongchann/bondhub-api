package com.bondhub.domain.transaction;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TransactionAggregationRepository extends JpaRepository<TransactionAggregation, Long> {

    Optional<TransactionAggregation> findByTransactionDate(String transactionDate);

    Optional<TransactionAggregation> findTopByTransactionDateOrderByCreatedDateTimeDesc(String transactionDate);
}