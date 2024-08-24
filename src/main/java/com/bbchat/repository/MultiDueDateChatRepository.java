package com.bbchat.repository;

import com.bbchat.domain.entity.MultiDueDateChat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MultiDueDateChatRepository extends JpaRepository<MultiDueDateChat, Long> {

    List<MultiDueDateChat> findByChatCreatedDate(String chatCreatedDate);
}
