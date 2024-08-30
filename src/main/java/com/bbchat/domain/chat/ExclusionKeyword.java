package com.bbchat.domain.chat;

import jakarta.persistence.*;
import lombok.*;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Getter
@Entity
public class ExclusionKeyword {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "exclusion_keyword_id")
    private Long id;

    private String name;

    public ExclusionKeyword(String name) {
        this.name = name;
    }
}
