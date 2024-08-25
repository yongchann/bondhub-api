package com.bbchat.repository;

import com.bbchat.domain.entity.Bond;
import com.bbchat.domain.entity.DailyAsk;
import com.bbchat.domain.entity.DailyTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface DailyTransactionRepository extends JpaRepository<DailyTransaction, Long> {

    @Query("SELECT da FROM DailyTransaction da WHERE da.bond = :bond AND da.createdDate = :date")
    Optional<DailyTransaction> findByBondAndDate(Bond bond, String date);

    void deleteAllByCreatedDate(String date);

    @Query("SELECT dt FROM DailyTransaction dt " +
            "JOIN FETCH dt.bond b " +
            "JOIN FETCH b.bondIssuer bi " +
            "WHERE dt.createdDate = :createdDate")
    List<DailyTransaction> findByCreatedDate(String createdDate);
}
