package com.bondhub.domain.bond;

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

    private String maturityDate;

    public Bond(BondIssuer bondIssuer, String maturityDate) {
        this.bondIssuer = bondIssuer;
        this.maturityDate = maturityDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Bond bond = (Bond) o;
        return Objects.equals(bondIssuer.getId(), bond.bondIssuer.getId()) &&
                Objects.equals(maturityDate, bond.maturityDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(bondIssuer.getId(), maturityDate);
    }

}
