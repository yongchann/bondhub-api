package com.bondhub.domain.chat;

import com.bondhub.domain.bond.BondIssuer;
import com.bondhub.domain.bond.BondType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

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

    private int maturityDateCount;

    private String triggerKeyword;

    private String senderName;

    private String senderAddress;

    public void initializeTradeType() {
        tradeType = TradeType.determineTypeFrom(content);
    }

    public void setMaturityDate(List<String> maturityDates) {
        if (maturityDates.size() > 1) {
            this.status = ChatStatus.NEEDS_SEPARATION;
        }
        this.maturityDateCount = maturityDates.size();
        this.maturityDate = String.join(",", maturityDates);
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

    public void setStatus(ChatStatus status) {
        this.status = status;
    }

    public static Chat fromMultiBondChat(Chat multiBondChat, String singleBondContent, String maturityDate) {
        return Chat.builder()
                .chatDateTime(multiBondChat.getChatDateTime())
                .senderName(multiBondChat.getSenderName())
                .content(singleBondContent)
                .senderAddress(multiBondChat.getSenderAddress())
                .status(ChatStatus.CREATED)
                .maturityDate(maturityDate)
                .build();
    }

    public void failedClassified() {
        this.status = ChatStatus.UNCATEGORIZED;
        this.triggerKeyword = "";
    }
}
