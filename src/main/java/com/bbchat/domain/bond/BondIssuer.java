package com.bbchat.domain.bond;

import jakarta.persistence.*;
import lombok.*;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Getter
@Entity
public class BondIssuer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "bond_issuer_id")
    private Long id;

    @Enumerated(EnumType.STRING)
    private BondType type;

    private String name;

    @Setter
    private String grade;

}
