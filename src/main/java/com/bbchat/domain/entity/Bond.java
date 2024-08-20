package com.bbchat.domain.entity;

import com.bbchat.domain.BondType;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Getter
@Entity
public class Bond extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "bond_id")
    private Long id;

    @Enumerated(EnumType.STRING)
    private BondType type;

    private String primaryName;

    @OneToMany(mappedBy = "bond", fetch = FetchType.LAZY)
    private Set<BondAlias> aliases = new HashSet<>();

}
