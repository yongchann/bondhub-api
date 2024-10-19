package com.bondhub.domain.chat;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface MultiBondChatRepository extends JpaRepository<MultiBondChat, Long> {

    Optional<MultiBondChat> findByChatDateTimeBetweenAndSenderNameAndContent(LocalDateTime start, LocalDateTime end, String senderName, String content);

}
