package com.bondhub.domain.chat;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Getter
@Entity
@Table(name = "multi_bond_chat", indexes = @Index(name = "idx_chat_date_time", columnList = "chat_date_time"))
public class MultiBondChat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "multi_bond_chat_id")
    private Long id;

    private LocalDateTime chatDateTime;

    @Column(name = "content", length = 2000)
    private String content;

    @Enumerated(EnumType.STRING)
    private ChatStatus status;

    private String maturityDate; // joined string with delimiter ','

    private int maturityDateCount;

    private String senderName;

    private String senderAddress;

    public void updateChatDateTime(LocalDateTime chatDateTime) {
        this.chatDateTime = chatDateTime;
    }

    public void completeSeparation() {
        this.status = ChatStatus.SEPARATED;
    }

}

