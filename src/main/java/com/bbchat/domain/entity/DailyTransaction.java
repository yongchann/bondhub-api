package com.bbchat.domain.entity;

import jakarta.persistence.*;
import lombok.*;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Getter
@Entity
public class DailyTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "daily_transaction_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "bond_id")
    private Bond bond;

    private String yield; // 민평수익율

    private String tradingYield; // 매매수익율

    private String spreadBp; // 민평대비(bp)

    private int consecutiveDays;

    private String createdDate;

}
