package com.bbchat.domain.aggregation;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Builder
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ChatAggregationResult {

    private String fileName;

    private LocalDateTime lastAggregatedDateTime;

    private long totalChatCount;

    private long notUsedChatCount;

    private long excludedChatCount;

    private long multiDueDateChatCount;

    private long uncategorizedChatCount;

    private long fullyProcessedChatCount;

    public static ChatAggregationResult from(ChatAggregation aggregation) {
        return ChatAggregationResult.builder()
                .fileName(aggregation.getFileName())
                .lastAggregatedDateTime(aggregation.getLastAggregatedDateTime())
                .totalChatCount(aggregation.getTotalChatCount())
                .notUsedChatCount(aggregation.getNotUsedChatCount())
                .excludedChatCount(aggregation.getExcludedChatCount())
                .multiDueDateChatCount(aggregation.getMultiDueDateChatCount())
                .uncategorizedChatCount(aggregation.getUncategorizedChatCount())
                .fullyProcessedChatCount(aggregation.getFullyProcessedChatCount())
                .build();
    }
}
