package com.bbchat.domain.aggregation;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Getter
@Entity
public class ChatAggregation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "chat_aggregation_id")
    private Long id;

    private String fileName;

    private String chatDate;

    private LocalDateTime lastAggregatedDateTime;

    private long totalChatCount;

    private long notUsedChatCount;

    private long excludedChatCount;

    private long multiDueDateChatCount;

    private long uncategorizedChatCount;

    private long fullyProcessedChatCount;

    public ChatAggregation(String fileName, String chatDate) {
        this.fileName = fileName;
        this.chatDate = chatDate;
    }

    public void update(LocalDateTime updateDateTime, ChatAggregationResult result) {
        this.lastAggregatedDateTime = updateDateTime;
        this.totalChatCount = result.getTotalChatCount();
        this.notUsedChatCount = result.getNotUsedChatCount();
        this.excludedChatCount = result.getExcludedChatCount();
        this.multiDueDateChatCount = result.getMultiDueDateChatCount();
        this.uncategorizedChatCount = result.getUncategorizedChatCount();
        this.fullyProcessedChatCount = result.getFullyProcessedChatCount();
    }
}
