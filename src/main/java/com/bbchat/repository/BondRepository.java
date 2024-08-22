package com.bbchat.repository;

import com.bbchat.domain.entity.Bond;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BondRepository extends JpaRepository<Bond, Long> {
}
