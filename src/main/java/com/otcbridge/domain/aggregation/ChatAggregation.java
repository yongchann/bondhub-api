package com.otcbridge.domain.aggregation;

import com.otcbridge.domain.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

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

}
