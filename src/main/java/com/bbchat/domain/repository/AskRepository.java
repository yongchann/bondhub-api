package com.bbchat.domain.repository;

import com.bbchat.domain.entity.Ask;
import com.bbchat.domain.entity.BondAlias;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AskRepository extends JpaRepository<Ask, Long> {
}
