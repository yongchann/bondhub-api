package com.otcbridge.repository;

import com.otcbridge.domain.aggregation.ChatAggregation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChatAggregationRepository extends JpaRepository<ChatAggregation, Long> {

    Optional<ChatAggregation> findTopByChatDateOrderByCreatedDateDesc(String chatDate);

    List<ChatAggregation> findByChatDateOrderByCreatedDateDesc(String chatDate);
}
