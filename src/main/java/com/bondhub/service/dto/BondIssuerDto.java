package com.bondhub.service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class BondIssuerDto {

    private Long id;

    private String name;

    private String type;

    private String grade;

    private List<BondAliasDto> bondAliases;
}
