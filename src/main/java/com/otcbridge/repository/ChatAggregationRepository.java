package com.otcbridge.repository;

import com.otcbridge.domain.aggregation.ChatAggregation;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ChatAggregationRepository extends JpaRepository<ChatAggregation, Long> {

    Optional<ChatAggregation> findTopByChatDateOrderByCreatedDateDesc(String chatDate);

    List<ChatAggregation> findByChatDateOrderByCreatedDateDesc(String chatDate);

    @Query("SELECT ca " +
            "FROM ChatAggregation ca " +
            "WHERE ca.chatDate = :chatDate")
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<ChatAggregation> findByChatDateWithPessimisticLock(String chatDate);

}
