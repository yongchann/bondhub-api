package com.bondhub.domain.bond;

import com.bondhub.domain.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Getter
@Entity
public class BondIssuer extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "bond_issuer_id")
    private Long id;

    @Enumerated(EnumType.STRING)
    private BondType type;

    private String name;

    private String grade;

    public void modify(String name, String grade) {
        this.name = name;
        this.grade = grade;
    }

}
