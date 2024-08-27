package com.bbchat.repository;

import com.bbchat.domain.bond.BondIssuer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BondIssuerRepository extends JpaRepository<BondIssuer, Long> {
}
