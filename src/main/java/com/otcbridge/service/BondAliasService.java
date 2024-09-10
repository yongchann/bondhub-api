package com.otcbridge.service;

import com.otcbridge.domain.bond.BondAlias;
import com.otcbridge.domain.bond.BondIssuer;
import com.otcbridge.repository.BondAliasRepository;
import com.otcbridge.repository.BondIssuerRepository;
import com.otcbridge.service.dto.BondAliasDto;
import com.otcbridge.service.event.BondAliasEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.Optional;

@RequiredArgsConstructor
@Service
public class BondAliasService {

    private final BondIssuerRepository bondIssuerRepository;
    private final BondAliasRepository bondAliasRepository;

    private final ApplicationEventPublisher eventPublisher;

    public BondAliasDto addBondAlias(Long bondIssuerId, String bondAliasName) {

        Optional<BondIssuer> bondIssuerOpt = bondIssuerRepository.findById(bondIssuerId);
        if (bondIssuerOpt.isEmpty()) {
            throw new IllegalArgumentException("BondIssuer with ID " + bondIssuerId + " does not exist.");
        }

        Optional<BondAlias> existingAliasOpt = bondAliasRepository.findByName(bondAliasName);
        if (existingAliasOpt.isPresent()) {
            throw new IllegalArgumentException("BondAlias with name " + bondAliasName + " already exists for BondIssuer ID " + bondIssuerId);
        }

        BondAlias newBondAlias = bondAliasRepository.save(BondAlias.builder()
                .name(bondAliasName)
                .bondIssuer(bondIssuerOpt.get())
                .build());

        eventPublisher.publishEvent(new BondAliasEvent(this, BondAliasEvent.Type.CREATED, "bond alias created", newBondAlias.getName()));

        return new BondAliasDto(newBondAlias.getId(), newBondAlias.getName());

    }

    public void deleteBondAlias(Long bondIssuerId, Long bondAliasId) {
        Optional<BondIssuer> bondIssuerOpt = bondIssuerRepository.findById(bondIssuerId);
        if (bondIssuerOpt.isEmpty()) {
            throw new IllegalArgumentException("BondIssuer with ID " + bondIssuerId + " does not exist.");
        }

        Optional<BondAlias> bondAliasOpt = bondAliasRepository.findById(bondAliasId);
        if (bondAliasOpt.isEmpty()) {
            throw new IllegalArgumentException("BondAlias with ID " + bondAliasId + " does not exist.");
        }

        BondAlias bondAlias = bondAliasOpt.get();
        if (!bondAlias.getBondIssuer().getId().equals(bondIssuerId)) {
            throw new IllegalArgumentException("BondAlias with ID " + bondAliasId + " does not belong to BondIssuer with ID " + bondIssuerId);
        }

        bondAliasRepository.deleteById(bondAliasId);

        eventPublisher.publishEvent(new BondAliasEvent(this, BondAliasEvent.Type.DELETED, "bond alias deleted", bondAlias.getName()));
    }

}
