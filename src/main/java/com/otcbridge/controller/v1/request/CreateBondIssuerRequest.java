package com.otcbridge.controller.v1.request;

import com.otcbridge.domain.bond.BondType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class CreateBondIssuerRequest {

    private BondType bondType;

    private String name;

    private String grade;
}
