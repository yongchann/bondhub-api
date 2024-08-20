package com.bbchat.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Getter
@Entity
public class AskRecord extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ask_record_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bond_id")
    private Bond bond;

    private String triggerTerm;

    private LocalDate dueDate;

    private String senderName;

    private String sendDateTime;

    private String originalContent;

    private String senderAddress;

}
