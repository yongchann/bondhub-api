package com.bbchat.domain;

public enum BondType {
    PUBLIC("공사채"),
    BANK("은행채"),
    SPECIALIZED_CREDIT("여전채"),
    COMPANY("회사채"),
    UNCATEGORIZED("미분류");

    BondType(String name) {
    }
}
