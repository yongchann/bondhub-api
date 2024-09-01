package com.bbchat.repository;

import com.bbchat.domain.chat.ExclusionKeyword;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ExclusionKeywordRepository extends JpaRepository<ExclusionKeyword, Long> {

    Optional<ExclusionKeyword> findByName(String name);
}
