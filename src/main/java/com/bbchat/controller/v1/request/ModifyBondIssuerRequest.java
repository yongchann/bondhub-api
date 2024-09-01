package com.bbchat.controller.v1.request;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class ModifyBondIssuerRequest {

    private String name;
    private String grade;
}
