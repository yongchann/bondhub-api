package com.bondhub.service;

import com.bondhub.domain.bond.BondAlias;
import com.bondhub.domain.bond.BondIssuer;
import com.bondhub.domain.bond.BondType;
import com.bondhub.domain.bond.BondAliasRepository;
import com.bondhub.domain.bond.BondIssuerRepository;
import com.bondhub.service.dto.BondAliasDto;
import com.bondhub.service.dto.BondIssuerDto;
import com.bondhub.service.event.BondAliasEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class BondIssuerService {

    private final BondIssuerRepository bondIssuerRepository;
    private final BondAliasRepository bondAliasRepository;

    private final ApplicationEventPublisher eventPublisher;

    public List<BondIssuerDto> getBondIssuersWithAliases() {

        List<BondAlias> bondAliases = bondAliasRepository.findAllFetchBond();
        Map<BondIssuer, List<BondAlias>> bondIssuerMap = bondAliases.stream()
                .collect(Collectors.groupingBy(BondAlias::getBondIssuer));

        // Convert to DTOs
        return bondIssuerMap.entrySet().stream()
                .map(entry -> {
                    BondIssuer bondIssuer = entry.getKey();
                    List<BondAliasDto> bondAliasDtos = entry.getValue().stream()
                            .map(bondAlias -> new BondAliasDto(bondAlias.getId(), bondAlias.getName()))
                            .collect(Collectors.toList());

                    return new BondIssuerDto(
                            bondIssuer.getId(),
                            bondIssuer.getName(),
                            bondIssuer.getType().name(),
                            bondIssuer.getGrade(),
                            bondAliasDtos
                    );
                })
                .sorted(Comparator.comparing(BondIssuerDto::getName))
                .collect(Collectors.toList());
    }

    @Transactional
    public void modify(Long bondIssuerId, String grade, String name) {
        BondIssuer bondIssuer = bondIssuerRepository.findById(bondIssuerId)
                .orElseThrow(() -> new NoSuchElementException("not found bond issuer id" + bondIssuerId));


        String originalName = bondIssuer.getName();

        BondAlias defaultBondAlias = bondAliasRepository.findByName(bondIssuer.getName())
                .orElseThrow(() -> new NoSuchElementException("not found bond alias name" + originalName));

        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("bond issuer name is empty");
        }

        if (grade == null || grade.trim().isEmpty()) {
            throw new IllegalArgumentException("bond issuer grade is empty");
        }

        bondIssuer.modify(name,grade);
        defaultBondAlias.changeName(name);

        eventPublisher.publishEvent(new BondAliasEvent(this, BondAliasEvent.Type.DELETED, "bond alias changed, original name: ", originalName));
        eventPublisher.publishEvent(new BondAliasEvent(this, BondAliasEvent.Type.CREATED, "bond alias changed, new name: ", name));
    }

    @Transactional
    public BondIssuerDto create(BondType bondType, String name, String grade) {
        Optional<BondIssuer> bondIssuer = bondIssuerRepository.findByName(name);
        if (bondIssuer.isPresent()) {
            throw new IllegalArgumentException("already exist issuer name:" + name);
        }

        BondIssuer newBondIssuer = BondIssuer.builder()
                .type(bondType)
                .name(name)
                .grade(grade).build();
        bondIssuerRepository.save(newBondIssuer);

        BondAlias defaultAlias = BondAlias.builder()
                .bondIssuer(newBondIssuer)
                .name(name)
                .build();
        bondAliasRepository.save(defaultAlias);

        eventPublisher.publishEvent(new BondAliasEvent(this, BondAliasEvent.Type.CREATED, "bond alias created", defaultAlias.getName()));

        return new BondIssuerDto(
                newBondIssuer.getId(),
                newBondIssuer.getName(),
                newBondIssuer.getType().name(),
                newBondIssuer.getGrade(),
                List.of(new BondAliasDto(defaultAlias.getId(), defaultAlias.getName()))
        );

    }
}
