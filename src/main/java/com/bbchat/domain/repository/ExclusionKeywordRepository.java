package com.bbchat.domain.repository;

import com.bbchat.domain.entity.ExclusionKeyword;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExclusionKeywordRepository extends JpaRepository<ExclusionKeyword, Long> {
}
