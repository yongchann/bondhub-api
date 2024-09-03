package com.bbchat.domain.chat;

import jakarta.persistence.*;
import lombok.*;

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

    private String originalContent;

    private String splitContents;

    public MultiBondChatHistory(String chatDate, String originalContent, String splitContents) {
        this.chatDate = chatDate;
        this.originalContent = originalContent;
        this.splitContents = splitContents;
    }
}
