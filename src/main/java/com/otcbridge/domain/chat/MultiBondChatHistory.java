package com.otcbridge.domain.chat;

import jakarta.persistence.*;
import lombok.*;

import java.util.Objects;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Getter
@Entity
public class MultiBondChatHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "multi_bond_chat_history_id")
    private Long id;

    private String chatDate;

    @Column(unique = true, length = 2000)
    private String originalContent;

    private String joinedContents;

    public MultiBondChatHistory(String chatDate, String originalContent, String joinedContents) {
        this.chatDate = chatDate;
        this.originalContent = originalContent;
        this.joinedContents = joinedContents;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MultiBondChatHistory that = (MultiBondChatHistory) o;
        return Objects.equals(originalContent, that.originalContent);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(originalContent);
    }
}

