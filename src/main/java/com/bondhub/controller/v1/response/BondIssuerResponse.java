package com.bondhub.controller.v1.response;

import com.bondhub.service.dto.BondIssuerDto;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
@Getter
public class BondIssuerResponse {

    private List<BondIssuerDto> publicIssuers;

    private List<BondIssuerDto> bankIssuers;

    private List<BondIssuerDto> specializedCreditIssuers;

    private List<BondIssuerDto> companyIssuers;

}
