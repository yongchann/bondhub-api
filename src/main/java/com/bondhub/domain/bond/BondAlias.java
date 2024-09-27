package com.bondhub.domain.bond;

import com.bondhub.domain.common.BaseTimeEntity;
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
    @JoinColumn(name = "bond_issuer_id")
    private BondIssuer bondIssuer;

    private String name;

    public void changeName(String name) {
        this.name = name;
    }
}
