package com.bbchat.repository;

import com.bbchat.domain.entity.Bond;
import com.bbchat.domain.entity.BondIssuer;
import com.bbchat.domain.entity.DailyAsk;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface DailyAskRepository extends JpaRepository<DailyAsk, Long> {

    @Query("SELECT da FROM DailyAsk da WHERE da.bond = :bond AND da.createdDate = :date")
    Optional<DailyAsk> findByBondAndDate(Bond bond, String date);

    @Query("SELECT da FROM DailyAsk da " +
            "JOIN FETCH da.bond b " +
            "JOIN FETCH b.bondIssuer bi " +
            "WHERE da.createdDate = :createdDate")
    List<DailyAsk> findByCreatedDate(String createdDate);
}
