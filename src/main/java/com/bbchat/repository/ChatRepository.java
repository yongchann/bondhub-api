package com.bbchat.repository;

import com.bbchat.domain.entity.Chat;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatRepository extends JpaRepository<Chat, Long> {

    void deleteAllByChatDate(String chatDate);

}
