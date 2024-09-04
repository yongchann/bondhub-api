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

    public void updateRetrialOfUncategorizedChat(long fullyProcessedChatCount) {
        this.uncategorizedChatCount -= fullyProcessedChatCount;
        this.fullyProcessedChatCount += fullyProcessedChatCount;
    }

    public void updateMultiDueDateSeparation(long uncategorizedChatCount, long fullyProcessedChatCount) {
        this.multiDueDateChatCount -= 1;
        this.uncategorizedChatCount += uncategorizedChatCount;
        this.fullyProcessedChatCount += fullyProcessedChatCount;
    }

    public void discardMultiDueDateChat(long cnt) {
        this.multiDueDateChatCount -= cnt;
    }

    public void discardUncategorizedChat(long cnt) {
        this.uncategorizedChatCount -= cnt;
    }

    public void updateResult(ChatAggregationResult result) {
        // 새 결과로 업데이트
        this.aggregatedDateTime = result.getAggregatedDateTime();
        this.totalChatCount = result.getTotalChatCount();

        // 이전 결과에 합산
        this.notUsedChatCount += result.getNotUsedChatCount();
        this.excludedChatCount += result.getExcludedChatCount();
        this.multiDueDateChatCount += result.getMultiDueDateChatCount();
        this.uncategorizedChatCount += result.getUncategorizedChatCount();
        this.fullyProcessedChatCount += result.getFullyProcessedChatCount();
    }
}
