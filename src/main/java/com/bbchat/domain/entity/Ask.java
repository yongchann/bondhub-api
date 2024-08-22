package com.bbchat.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.Objects;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Getter
@Entity
public class Ask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ask_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bond_id")
    private Bond bond;

    private String triggerTerm;

    private String dueDate;

    private String originalContent;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Ask ask = (Ask) o;
        return Objects.equals(bond, ask.bond) &&
                Objects.equals(dueDate, ask.dueDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(bond, dueDate);
    }
}
