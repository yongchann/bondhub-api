package com.bbchat.service;

import com.bbchat.domain.entity.BondAlias;
import com.bbchat.domain.entity.BondIssuer;
import com.bbchat.repository.BondAliasRepository;
import com.bbchat.service.dto.BondAliasDto;
import com.bbchat.service.dto.BondIssuerDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class BondIssuerService {

    private final BondAliasRepository bondAliasRepository;

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
                            bondAliasDtos
                    );
                })
                .sorted(Comparator.comparing(BondIssuerDto::getName))
                .collect(Collectors.toList());
    }

}
