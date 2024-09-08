package com.bbchat.domain.bond;

import jakarta.persistence.*;
import lombok.*;

import java.util.Objects;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Getter
@Entity
public class Bond {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "bond_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bond_issuer_id")
    private BondIssuer bondIssuer;

    private String dueDate;

    public Bond(BondIssuer bondIssuer, String dueDate) {
        this.bondIssuer = bondIssuer;
        this.dueDate = dueDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Bond bond = (Bond) o;
        return Objects.equals(bondIssuer.getId(), bond.bondIssuer.getId()) &&
                Objects.equals(dueDate, bond.dueDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(bondIssuer.getId(), dueDate);
    }

}
