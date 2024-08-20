package com.bbchat.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Getter
@Entity
public class DailyAskAggregation extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "daily_ask_aggregation_id")
    private Long id;

    private LocalDate date;

    private String filename;

    private String jsonAskData;

}
