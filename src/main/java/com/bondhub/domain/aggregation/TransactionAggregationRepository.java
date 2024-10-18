package com.bondhub.domain.aggregation;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TransactionAggregationRepository extends JpaRepository<TransactionAggregation, Long> {

    Optional<TransactionAggregation> findTopByTransactionDateOrderByCreatedDateTimeDesc(String transactionDate);
}
