package com.bondhub.domain.chat;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;

@AllArgsConstructor
@Getter
public enum TradeType {
    SELL(List.of("팔자", "팔고", "팔거나", "매도 관심", "매도관심")),
    SWAP(List.of("교체", "교체 관심", "교체관심")),
    BUY(List.of("사자", "매수 관심", "매수관심")),
    UNCATEGORIZED(List.of());

    private final List<String> keywords;

    public static TradeType determineTypeFrom(String chatContent) {
        return Arrays.stream(TradeType.values())
                .filter(type -> type.getKeywords().stream().anyMatch(chatContent::contains))
                .findFirst()
                .orElse(UNCATEGORIZED);
    }

}
