package com.otcbridge.service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@AllArgsConstructor
@Setter
@Getter
public class BondIssuerJson {
    private String grade;
    private List<String> aliases;

}
