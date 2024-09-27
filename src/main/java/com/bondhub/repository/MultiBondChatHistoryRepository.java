package com.bondhub.repository;

import com.bondhub.domain.chat.MultiBondChatHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MultiBondChatHistoryRepository extends JpaRepository<MultiBondChatHistory, Long> {

    Optional<MultiBondChatHistory> findByOriginalContent(String originalContent);
}
