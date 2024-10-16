package com.bondhub.domain.bond;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum BondType {
    PUBLIC("공사채", "크레딧"),
    COMMERCIAL_BANK("시중은행채", "크레딧"),
    SPECIAL_BANK("특수은행채", "크레딧"),
    CARD("카드채", "크레딧"),
    CAPITAL("캐피탈채", "크레딧"),
    COMPANY("회사채", "크레딧"),

    KTB("국고채", "국고채권"),
    KNHB("국민주택채", "국고채권"),

    MSB("통안채", "통화안정채권"),

    CD("CD", "단기채권"),
    ABSTB("ABSTB", "단기채권"),
    ABCP("ABCP", "단기채권"),
    CP("CP", "단기채권"),
    STB("전단채", "단기채권"),

    FRN("FRN", "변동금리채권");

    private final String name;
    private final String description;
}
