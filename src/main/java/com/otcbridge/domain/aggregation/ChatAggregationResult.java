package com.otcbridge.domain.aggregation;

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

    private long multiDueDateChatCount;

    private long uncategorizedChatCount;

    private long fullyProcessedChatCount;

    public static ChatAggregationResult from(ChatAggregation entity) {
        return ChatAggregationResult.builder()
                .aggregatedDateTime(entity.getModifiedDateTime())
                .totalChatCount(entity.getTotalChatCount())
                .notUsedChatCount(entity.getNotUsedChatCount())
                .multiDueDateChatCount(entity.getMultiDueDateChatCount())
                .uncategorizedChatCount(entity.getUncategorizedChatCount())
                .fullyProcessedChatCount(entity.getFullyProcessedChatCount())
                .build();
    }
}
