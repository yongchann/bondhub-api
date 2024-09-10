package com.otcbridge.service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Objects;

@AllArgsConstructor
@Getter
public class BondGradeCollisionDto {

    private Long bondIssuerId;

    private String bondIssuerName;

    private String bondIssuerGrade;

    private String transactionBondName;

    private String transactionGrade;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BondGradeCollisionDto that = (BondGradeCollisionDto) o;
        return Objects.equals(bondIssuerId, that.bondIssuerId) && Objects.equals(transactionBondName, that.transactionBondName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(bondIssuerId, transactionBondName);
    }
}
