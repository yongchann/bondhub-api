package com.bondhub.domain.bond;

public enum BondType {
    PUBLIC("공사채"),
    COMMERCIAL_BANK("시중은행채"),
    SPECIAL_BANK("특수은행채"),
    CARD("카드채"),
    CAPITAL("캐피탈채"),
    COMPANY("회사채"),
    BANK("은행채"),
    SPECIALIZED_CREDIT("여전채");

    BondType(String name) {
    }
}
