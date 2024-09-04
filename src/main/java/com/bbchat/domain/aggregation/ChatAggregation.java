package com.bbchat.domain.aggregation;

import jakarta.persistence.*;
import lombok.*;

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

    private String chatDate;

    private String roomType;

    @Embedded
    private ChatAggregationResult result;

    public static ChatAggregation createWithEmptyResult(String chatDate, String roomType) {
        return ChatAggregation.builder()
                .chatDate(chatDate)
                .roomType(roomType)
                .result(new ChatAggregationResult())
                .build();
    }
}
