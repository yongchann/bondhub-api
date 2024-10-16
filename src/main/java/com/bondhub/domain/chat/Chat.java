package com.bondhub.domain.chat;

import com.bondhub.domain.bond.BondIssuer;
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

    private String chatDate;

    private String sendTime;

    private String senderName;

    @Enumerated(EnumType.STRING)
    private TradeType tradeType = TradeType.UNCATEGORIZED;

    @Enumerated(EnumType.STRING)
    private ChatStatus status = ChatStatus.UNCATEGORIZED;

    private String maturityDate;

    private int maturityDateCount;

    @Column(name = "content", length = 2000)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bond_issuer_id")
    private BondIssuer bondIssuer;

    private String triggerKeyword;

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
