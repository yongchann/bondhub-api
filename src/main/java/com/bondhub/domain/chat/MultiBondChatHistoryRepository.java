package com.bondhub.domain.chat;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MultiBondChatHistoryRepository extends JpaRepository<MultiBondChatHistory, Long> {

    Optional<MultiBondChatHistory> findByOriginalContent(String originalContent);
}
