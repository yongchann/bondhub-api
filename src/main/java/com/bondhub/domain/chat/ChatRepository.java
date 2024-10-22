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
            "AND c.bondType = :bondType " +
            "AND c.tradeType = 'SELL'" +
            "AND c.status = 'OK'" +
            "AND LENGTH(c.maturityDate) > 0")
    List<Chat> findByChatDateTimeBetweenAndBondIssuerType(LocalDateTime start, LocalDateTime end, BondType bondType);

    List<Chat> findByChatDateTimeBetweenAndStatus(LocalDateTime start, LocalDateTime end, ChatStatus status);

    @Query("SELECT c FROM Chat c " +
            "JOIN FETCH c.bondIssuer bi " +
            "JOIN (SELECT c2.bondIssuer.id as bondIssuerId, c2.maturityDate as maturityDate, MAX(c2.chatDateTime) as latestChatDateTime " +
            "      FROM Chat c2 " +
            "      WHERE c2.tradeType = 'SELL' " +
            "      AND c2.status = 'OK' " +
            "      AND LENGTH(c2.maturityDate) > 0 " +
            "      AND c2.bondType = :bondType " +
            "      AND c2.chatDateTime BETWEEN :start AND :end " +  // start와 end 조건 추가
            "      GROUP BY c2.bondIssuer.id, c2.maturityDate) latest " +
            "ON c.bondIssuer.id = latest.bondIssuerId " +
            "AND c.maturityDate = latest.maturityDate " +
            "AND c.chatDateTime = latest.latestChatDateTime " +
            "WHERE c.tradeType = 'SELL' " +
            "AND c.status = 'OK' " +
            "AND LENGTH(c.maturityDate) > 0 " +
            "AND c.bondType = :bondType " +
            "AND c.chatDateTime BETWEEN :start AND :end")  // start와 end 조건 추가
    List<Chat> findLatestAsksByBond(LocalDateTime start, LocalDateTime end, BondType bondType);



}

