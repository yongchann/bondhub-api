package com.bbchat.domain.aggregation;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Builder
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ChatAggregationResult {

    private LocalDateTime aggregatedDateTime;

    private long totalChatCount;

    private long notUsedChatCount;

    private long excludedChatCount;

    private long multiDueDateChatCount;

    private long uncategorizedChatCount;

    private long fullyProcessedChatCount;

    public static ChatAggregationResult from(ChatAggregation entity) {
        return ChatAggregationResult.builder()
                .aggregatedDateTime(entity.getResult().getAggregatedDateTime())
                .totalChatCount(entity.getResult().getTotalChatCount())
                .notUsedChatCount(entity.getResult().getNotUsedChatCount())
                .excludedChatCount(entity.getResult().getExcludedChatCount())
                .multiDueDateChatCount(entity.getResult().getMultiDueDateChatCount())
                .uncategorizedChatCount(entity.getResult().getUncategorizedChatCount())
                .fullyProcessedChatCount(entity.getResult().getFullyProcessedChatCount())
                .build();
    }
}
