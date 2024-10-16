package com.bondhub.domain.ask;

import com.bondhub.domain.bond.BondIssuer;
import lombok.*;

import java.util.Objects;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Getter
public class Bond {

    private BondIssuer bondIssuer;

    private String maturityDate;

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
