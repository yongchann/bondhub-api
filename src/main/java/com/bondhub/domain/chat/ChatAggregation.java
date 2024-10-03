package com.bondhub.domain.chat;

import com.bondhub.domain.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.Map;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Getter
@Entity
public class ChatAggregation extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "chat_aggregation_id")
    private Long id;

    private String chatDate;

    private long totalChatCount;

    private long notUsedChatCount;

    private long multiDueDateChatCount;

    private long uncategorizedChatCount;

    private long fullyProcessedChatCount;

    public static ChatAggregation create(String chatDate) {
        return ChatAggregation.builder()
                .chatDate(chatDate)
                .build();
    }

    public void update(Map<ChatStatus, Long> statusCounts) {
        long totalCount = statusCounts.values().stream()
                .mapToLong(Long::longValue)
                .sum();

        this.totalChatCount += totalCount;
        this.notUsedChatCount += statusCounts.getOrDefault(ChatStatus.CREATED, 0L);
        this.multiDueDateChatCount += statusCounts.getOrDefault(ChatStatus.MULTI_DD, 0L);
        this.uncategorizedChatCount += statusCounts.getOrDefault(ChatStatus.UNCATEGORIZED, 0L);
        this.fullyProcessedChatCount += statusCounts.getOrDefault(ChatStatus.OK, 0L);
    }

    public void updateRetrialOfUncategorizedChat(long fullyProcessedChatCount) {
        this.uncategorizedChatCount -= fullyProcessedChatCount;
        this.fullyProcessedChatCount += fullyProcessedChatCount;
    }

    public void updateMultiDueDateSeparation(long multiBondChatCount, long uncategorizedChatCount, long fullyProcessedChatCount) {
        this.multiDueDateChatCount -= multiBondChatCount;
        this.uncategorizedChatCount += uncategorizedChatCount;
        this.fullyProcessedChatCount += fullyProcessedChatCount;
    }

    public void discardMultiDueDateChat(long cnt) {
        this.multiDueDateChatCount -= cnt;
    }

    public void discardUncategorizedChat(long cnt) {
        this.uncategorizedChatCount -= cnt;
    }

}
