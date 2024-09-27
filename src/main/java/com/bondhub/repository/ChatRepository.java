package com.bondhub.repository;

import com.bondhub.domain.bond.BondType;
import com.bondhub.domain.chat.Chat;
import com.bondhub.domain.chat.ChatStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ChatRepository extends JpaRepository<Chat, Long> {

    @Modifying
    @Query("DELETE FROM Chat c WHERE c.chatDate = :chatDate")
    int deleteAllByChatDateInBatch(String chatDate);

    @Query("SELECT c FROM Chat c " +
            "JOIN FETCH c.bond b " +
            "JOIN FETCH b.bondIssuer bi " +
            "WHERE c.chatDate = :date " +
            "AND c.status = 'OK' " +
            "AND bi.type = :bondType " +
            "AND c.dueDate BETWEEN :startDueDate AND :endDueDate " +
            "AND bi.grade IN :grades " +
            "ORDER BY b.dueDate ASC, c.sendDateTime DESC")
    List<Chat> findValidChatsWithinDueDateRangeAndIssuerGrades(
            @Param("date") String date,
            @Param("bondType") BondType bondType,
            @Param("startDueDate") String startDueDate,
            @Param("endDueDate") String endDueDate,
            @Param("grades") List<String> grades
    );

    @Query("SELECT c FROM Chat c " +
            "JOIN FETCH c.bond b " +
            "JOIN FETCH b.bondIssuer bi " +
            "WHERE c.chatDate = :chatDate " +
            "AND c.status = 'OK'")
    List<Chat> findFetchBondAndBondIssuerByChatDate(String chatDate);

    List<Chat> findByChatDateAndStatus(String chatDate, ChatStatus status);

    List<Chat> findByChatDateAndStatusAndIdIn(String chatDate, ChatStatus status, List<Long> ids);

    Optional<Chat> findByIdAndChatDateAndStatus(Long id, String chatDate, ChatStatus status);
}

