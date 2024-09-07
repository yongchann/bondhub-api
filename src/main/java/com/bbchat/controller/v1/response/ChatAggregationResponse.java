package com.bbchat.controller.v1.response;

import com.bbchat.domain.aggregation.ChatAggregationResult;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Builder
@AllArgsConstructor
@Getter
public class ChatAggregationResponse {

    private LocalDateTime lastAggregatedDateTime;

    private long totalChatCount;

    private long excludedChatCount; // 제외 키워드에 의해 제거된 채팅

    private long notContainDueDateChatCount; // 제외되지 않았으나 만기를 포함하지 않은 채팅

    private long fullyProcessedChatCount;

    private long multiDueDateChatCount; // 수동 처리 필요

    private long uncategorizedChatCount; // 수동 처리 필요

    public static ChatAggregationResponse from(ChatAggregationResult result) {
        return ChatAggregationResponse.builder()
                .lastAggregatedDateTime(result.getAggregatedDateTime())
                .totalChatCount(result.getTotalChatCount())
                .notContainDueDateChatCount(result.getNotUsedChatCount())
                .fullyProcessedChatCount(result.getFullyProcessedChatCount())
                .multiDueDateChatCount(result.getMultiDueDateChatCount())
                .uncategorizedChatCount(result.getUncategorizedChatCount())
                .build();
    }

}
