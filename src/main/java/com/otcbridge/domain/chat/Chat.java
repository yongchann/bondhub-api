package com.otcbridge.domain.chat;

import com.otcbridge.domain.bond.Bond;
import jakarta.persistence.*;
import lombok.*;

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

    private String chatDate;

    private String senderName;

    private String sendDateTime;

    @Enumerated(EnumType.STRING)
    private ChatStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bond_id")
    private Bond bond;

    private String dueDate;

    @Column(name = "content", length = 2000)
    private String content;

    private String senderAddress;

    public void modifyStatusByDueDate(List<String> dueDateInContent) {
        if (dueDateInContent.size() == 1) {
            status = ChatStatus.SINGLE_DD;
            dueDate = dueDateInContent.get(0);
        } else if (dueDateInContent.size() >= 2) {
            status = ChatStatus.MULTI_DD;
            dueDate = "";
        }
    }

    public void setStatus(ChatStatus status) {
        this.status = status;
    }

    public void setBond(Bond bond) {
        this.bond = bond;
    }

    public static Chat fromMultiBondChat(Chat multiBondChat, String singleBondContent, String dueDate) {
        return Chat.builder()
                .chatDate(multiBondChat.getChatDate())
                .senderName(multiBondChat.getSenderName())
                .sendDateTime(multiBondChat.getSendDateTime())
                .content(singleBondContent)
                .senderAddress(multiBondChat.getSenderAddress())
                .status(ChatStatus.SINGLE_DD)
                .dueDate(dueDate)
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
