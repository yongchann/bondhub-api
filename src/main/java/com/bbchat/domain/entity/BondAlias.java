package com.bbchat.domain.entity;

import jakarta.persistence.*;
import lombok.*;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Getter
@Entity
public class BondAlias extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "bond_alias_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "bond_id")
    private Bond bond;

    private String name;
}
