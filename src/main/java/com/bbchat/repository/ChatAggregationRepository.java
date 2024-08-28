package com.bbchat.repository;

import com.bbchat.domain.aggregation.ChatAggregation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ChatAggregationRepository extends JpaRepository<ChatAggregation, Long> {

    Optional<ChatAggregation> findByChatDate(String chatDate);
}
