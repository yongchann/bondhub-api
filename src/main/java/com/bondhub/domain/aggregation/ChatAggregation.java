package com.bondhub.domain.aggregation;

import com.bondhub.domain.chat.Chat;
import com.bondhub.domain.chat.ChatStatus;
import com.bondhub.domain.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    public void update(List<Chat> singleBondChats, int multiBondChatsSize) {
        Map<ChatStatus, Long> groupByStatus = singleBondChats.stream()
                .collect(Collectors.groupingBy(Chat::getStatus, Collectors.counting()));

        this.totalChatCount += singleBondChats.size() + multiBondChatsSize;
        this.multiDueDateChatCount += multiBondChatsSize;
        this.notUsedChatCount += groupByStatus.getOrDefault(ChatStatus.CREATED, 0L);
        this.uncategorizedChatCount += groupByStatus.getOrDefault(ChatStatus.UNCATEGORIZED, 0L);
        this.fullyProcessedChatCount += groupByStatus.getOrDefault(ChatStatus.OK, 0L);
    }

    public void updateSeparation(List<Chat> separatedChats) {
        Map<ChatStatus, Long> groupByStatus = separatedChats.stream()
                .collect(Collectors.groupingBy(Chat::getStatus, Collectors.counting()));

        this.multiDueDateChatCount -= 1;
        this.notUsedChatCount += groupByStatus.getOrDefault(ChatStatus.CREATED, 0L);
        this.uncategorizedChatCount += groupByStatus.getOrDefault(ChatStatus.UNCATEGORIZED, 0L);
        this.fullyProcessedChatCount += groupByStatus.getOrDefault(ChatStatus.OK, 0L);
    }

    public void updateRetrial(List<Chat> uncategorizedChats) {
        Map<ChatStatus, Long> groupByStatus = uncategorizedChats.stream()
                .collect(Collectors.groupingBy(Chat::getStatus, Collectors.counting()));

        Long success = groupByStatus.getOrDefault(ChatStatus.OK, 0L);
        this.uncategorizedChatCount -= success;
        this.fullyProcessedChatCount += success;
    }

}
