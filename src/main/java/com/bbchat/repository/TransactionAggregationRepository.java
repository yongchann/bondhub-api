package com.bbchat.repository;

import com.bbchat.domain.aggregation.ChatAggregation;
import com.bbchat.domain.aggregation.TransactionAggregation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TransactionAggregationRepository extends JpaRepository<TransactionAggregation, Long> {

    Optional<TransactionAggregation> findByTransactionDate(String transactionDate);

    Optional<TransactionAggregation> findTopByTransactionDateOrderByCreatedDateDesc(String transactionDate);
}
