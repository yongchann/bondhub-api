package com.bondhub.domain.chat;

import com.bondhub.domain.bond.BondType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface ChatRepository extends JpaRepository<Chat, Long> {

    @Modifying
    int deleteByChatDateTimeBetween(LocalDateTime start, LocalDateTime end);

    List<Chat> findByChatDateTimeBetweenAndIdIn(LocalDateTime start, LocalDateTime end, List<Long> ids);

    @Query("SELECT c " +
            "FROM Chat c " +
            "LEFT JOIN FETCH c.bondIssuer bi " +
            "WHERE c.chatDateTime BETWEEN :start AND :end " +
            "AND c.bondType = :bondType")
    List<Chat> findByChatDateTimeBetweenAndBondIssuerType(LocalDateTime start, LocalDateTime end, BondType bondType);

    List<Chat> findByChatDateTimeBetweenAndStatus(LocalDateTime start, LocalDateTime end, ChatStatus status);
}

