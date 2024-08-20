package com.bbchat.domain.repository;

import com.bbchat.domain.entity.BondAlias;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface BondAliasRepository extends JpaRepository<BondAlias, Long> {

    @Query("SELECT ba FROM BondAlias ba JOIN FETCH ba.bond")
    List<BondAlias> findAllFetchBond();
}
