package com.bondhub.domain.aggregation;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface ChatAggregationRepository extends JpaRepository<ChatAggregation, Long> {

    Optional<ChatAggregation> findByChatDate(String chatDate);

    @Query("SELECT ca " +
            "FROM ChatAggregation ca " +
            "WHERE ca.chatDate = :chatDate")
    @Lock(LockModeType.PESSIMISTIC_READ)
    Optional<ChatAggregation> findByChatDateWithPessimisticLock(String chatDate);

}
