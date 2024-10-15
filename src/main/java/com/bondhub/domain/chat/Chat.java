package com.bondhub.domain.chat;

import com.bondhub.domain.bond.Bond;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Getter
@Entity
public class Chat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "chat_id")
    private Long id;

    private LocalDateTime chatDateTime;

    private String chatDate;

    private String senderName;

    private String sendTime;

    @Enumerated(EnumType.STRING)
    private TradeType tradeType;

    @Enumerated(EnumType.STRING)
    private ChatStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bond_id")
    private Bond bond;

    private String maturityDate;

    @Column(name = "content", length = 2000)
    private String content;

    private String senderAddress;

    public void modifyStatusByMaturityDate(List<String> maturityDateInContent) {
        if (maturityDateInContent.size() == 1) {
            status = ChatStatus.SINGLE_DD;
            maturityDate = maturityDateInContent.get(0);
        } else if (maturityDateInContent.size() >= 2) {
            status = ChatStatus.MULTI_DD;
            maturityDate = "";
        }
    }

    public void setStatus(ChatStatus status) {
        this.status = status;
    }

    public void setBond(Bond bond) {
        this.bond = bond;
    }

    public static Chat fromMultiBondChat(Chat multiBondChat, String singleBondContent, String maturityDate) {
        return Chat.builder()
                .chatDateTime(multiBondChat.getChatDateTime())
                .senderName(multiBondChat.getSenderName())
                .content(singleBondContent)
                .senderAddress(multiBondChat.getSenderAddress())
                .status(ChatStatus.SINGLE_DD)
                .maturityDate(maturityDate)
                .build();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Chat chat = (Chat) o;
        return Objects.equals(content, chat.content);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(content);
    }
}
