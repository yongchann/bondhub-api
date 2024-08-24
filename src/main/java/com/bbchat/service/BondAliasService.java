package com.bbchat.service;

import com.bbchat.domain.entity.BondAlias;
import com.bbchat.domain.entity.BondIssuer;
import com.bbchat.repository.BondAliasRepository;
import com.bbchat.repository.BondIssuerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class BondAliasService {

    private final BondIssuerRepository bondIssuerRepository;
    private final BondAliasRepository bondAliasRepository;

    public void addBondAlias(Long bondIssuerId, String bondAliasName) {

        Optional<BondIssuer> bondIssuerOpt = bondIssuerRepository.findById(bondIssuerId);
        if (bondIssuerOpt.isEmpty()) {
            throw new IllegalArgumentException("BondIssuer with ID " + bondIssuerId + " does not exist.");
        }

        Optional<BondAlias> existingAliasOpt = bondAliasRepository.findByNameAndBondIssuerId(bondAliasName, bondIssuerId);
        if (existingAliasOpt.isPresent()) {
            throw new IllegalArgumentException("BondAlias with name " + bondAliasName + " already exists for BondIssuer ID " + bondIssuerId);
        }

        bondAliasRepository.save(BondAlias.builder()
                .name(bondAliasName)
                .bondIssuer(bondIssuerOpt.get())
                .build());
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
    }

}
