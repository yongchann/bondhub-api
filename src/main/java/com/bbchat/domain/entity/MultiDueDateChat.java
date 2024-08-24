package com.bbchat.domain.entity;

import jakarta.persistence.*;
import lombok.*;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Getter
@Entity
public class MultiDueDateChat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "multi_due_date_chat_id")
    private Long id;

    private String content;

    private String chatCreatedDate;

    @Enumerated(EnumType.STRING)
    private ChatStatus status;

    private String senderName;

    private String sendDateTime;

    private String senderAddress;


}
