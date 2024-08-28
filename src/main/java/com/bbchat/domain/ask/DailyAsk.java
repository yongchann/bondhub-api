package com.bbchat.domain.ask;

import com.bbchat.domain.bond.Bond;
import jakarta.persistence.*;
import lombok.*;

import java.util.Objects;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Getter
@Entity
public class DailyAsk {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "daily_ask_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "bond_id")
    private Bond bond;

    private int consecutiveDays;

    private String createdDate;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DailyAsk dailyAsk = (DailyAsk) o;
        return Objects.equals(bond, dailyAsk.bond) && Objects.equals(createdDate, dailyAsk.createdDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(bond, createdDate);
    }
}
