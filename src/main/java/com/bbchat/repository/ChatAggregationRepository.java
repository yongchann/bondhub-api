package com.bbchat.repository;

import com.bbchat.domain.aggregation.ChatAggregation;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface ChatAggregationRepository extends JpaRepository<ChatAggregation, Long> {

    Optional<ChatAggregation> findByChatDateAndRoomType(String chatDate, String roomType);

    @Query("SELECT ca " +
            "FROM ChatAggregation ca " +
            "WHERE ca.chatDate = :chatDate AND ca.roomType = :roomType")
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<ChatAggregation> findByChatDateAndRoomTypeWithPessimisticLock(String chatDate, String roomType);
}
