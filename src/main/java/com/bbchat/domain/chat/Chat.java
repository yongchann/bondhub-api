package com.bbchat.domain.chat;

import com.bbchat.domain.bond.Bond;
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

    private String roomType;

    private String chatDate;

    private String senderName;

    private String sendDateTime;

    @Enumerated(EnumType.STRING)
    private ChatStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bond_id")
    private Bond bond;

    private String dueDate;

    private String content;

    private String senderAddress;

    public void modifyStatusByDueDate(List<String> dueDateInContent) {
        if (dueDateInContent.isEmpty()) {
            status = ChatStatus.NOT_USED;
            dueDate = "";
        } else if (dueDateInContent.size() == 1) {
            status = ChatStatus.SINGLE_DD;
            dueDate = dueDateInContent.get(0);
        } else {
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
