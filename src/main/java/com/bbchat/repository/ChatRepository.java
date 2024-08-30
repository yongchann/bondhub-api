package com.bbchat.repository;

import com.bbchat.domain.entity.Chat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ChatRepository extends JpaRepository<Chat, Long> {

    void deleteAllByChatDateAndRoomType(String chatDate, String roomType);

    @Query("SELECT c FROM Chat c " +
            "JOIN FETCH c.bond b " +
            "JOIN FETCH b.bondIssuer bi " +
            "WHERE c.chatDate = :date " +
            "AND c.status = 'OK' " +
            "AND c.dueDate BETWEEN :startDueDate AND :endDueDate " +
            "AND bi.grade IN :grades " +
            "ORDER BY b.dueDate ASC, c.sendDateTime DESC")
    List<Chat> findValidChatsWithinDueDateRangeAndIssuerGrades(
            @Param("date") String date,
            @Param("startDueDate") String startDueDate,
            @Param("endDueDate") String endDueDate,
            @Param("grades") List<String> grades
    );

}

