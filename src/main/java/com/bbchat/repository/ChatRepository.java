package com.bbchat.repository;

import com.bbchat.domain.bond.BondType;
import com.bbchat.domain.chat.Chat;
import com.bbchat.domain.chat.ChatStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ChatRepository extends JpaRepository<Chat, Long> {

    void deleteAllByChatDateAndRoomType(String chatDate, String roomType);

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

    List<Chat> findByChatDateAndRoomTypeAndStatus(String chatDate, String roomType, ChatStatus status);

    List<Chat> findByChatDateAndRoomTypeAndStatusAndIdIn(String chatDate, String roomType, ChatStatus status, List<Long> ids);

    Optional<Chat> findByIdAndChatDateAndRoomTypeAndStatus(Long id, String chatDate, String roomType, ChatStatus status);
}

