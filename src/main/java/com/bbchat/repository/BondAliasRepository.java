package com.bbchat.repository;

import com.bbchat.domain.entity.BondAlias;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BondAliasRepository extends JpaRepository<BondAlias, Long> {

    @Query("SELECT ba FROM BondAlias ba JOIN FETCH ba.bondIssuer")
    List<BondAlias> findAllFetchBond();

    @Query("SELECT ba FROM BondAlias ba WHERE ba.name = :name AND ba.bondIssuer.id = :bondIssuerId")
    Optional<BondAlias> findByNameAndBondIssuerId(@Param("name") String name, @Param("bondIssuerId") Long bondIssuerId);

}
