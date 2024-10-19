package com.bondhub.domain.chat;

import com.bondhub.domain.bond.BondIssuer;
import com.bondhub.domain.bond.BondType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Getter
@Entity
@Table(name = "chat", indexes = @Index(name = "idx_chat_date_time", columnList = "chat_date_time"))
public class Chat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "chat_id")
    private Long id;

    private LocalDateTime chatDateTime;

    @Column(name = "content", length = 2000)
    private String content;

    @Enumerated(EnumType.STRING)
    private TradeType tradeType;

    @Enumerated(EnumType.STRING)
    private ChatStatus status;

    @Enumerated(EnumType.STRING)
    private BondType bondType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bond_issuer_id")
    private BondIssuer bondIssuer;

    private String maturityDate; // joined string with delimiter ','

    private String triggerKeyword;

    private String senderName;

    private String senderAddress;

    public void initializeTradeType() {
        tradeType = TradeType.determineTypeFrom(content);
    }

    public void classified(BondIssuer bondIssuer, String triggerKeyword) {
        this.bondIssuer = bondIssuer;
        this.triggerKeyword = triggerKeyword;
        this.status = ChatStatus.OK;
        this.bondType = bondIssuer.getType();
    }

    public void classified(BondType bondType, String triggerKeyword) {
        this.triggerKeyword = triggerKeyword;
        this.status = ChatStatus.OK;
        this.bondType = bondType;
    }

    public void discard() {
        this.status = ChatStatus.DISCARDED;
    }

    public static Chat fromSeparation(MultiBondChat originalChat, ChatSeparationResult result) {
        return Chat.builder()
                .chatDateTime(originalChat.getChatDateTime())
                .senderName(originalChat.getSenderName())
                .senderAddress(originalChat.getSenderAddress())
                .content(result.content())
                .maturityDate(result.maturityDate())
                .status(ChatStatus.CREATED)
                .build();
    }

    public void failedClassified() {
        this.status = ChatStatus.UNCATEGORIZED;
        this.triggerKeyword = "";
    }
}
