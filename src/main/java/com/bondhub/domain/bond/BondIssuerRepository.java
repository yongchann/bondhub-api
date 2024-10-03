package com.bondhub.domain.bond;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BondIssuerRepository extends JpaRepository<BondIssuer, Long> {

    Optional<BondIssuer> findByName(String name);
}
