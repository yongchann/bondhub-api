package com.bondhub.domain.bond;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BondRepository extends JpaRepository<Bond, Long> {

    Optional<Bond> findByBondIssuerAndDueDate(BondIssuer bondIssuer, String dueDate);
}