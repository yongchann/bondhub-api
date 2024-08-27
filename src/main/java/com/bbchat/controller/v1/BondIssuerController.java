package com.bbchat.controller.v1;

import com.bbchat.controller.v1.response.BondIssuerResponse;
import com.bbchat.domain.bond.BondType;
import com.bbchat.service.BondAliasService;
import com.bbchat.service.BondIssuerService;
import com.bbchat.service.dto.BondIssuerDto;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@RestController
public class BondIssuerController {

    private final BondIssuerService bondIssuerService;
    private final BondAliasService bondAliasService;

    @GetMapping("/api/v1/issuer/all")
    public BondIssuerResponse getBondsByType() {
        List<BondIssuerDto> bondIssuersWithAliases = bondIssuerService.getBondIssuersWithAliases();

        Map<BondType, List<BondIssuerDto>> bondIssuerMap = bondIssuersWithAliases.stream()
                .collect(Collectors.groupingBy(dto -> BondType.valueOf(dto.getType())));

        return new BondIssuerResponse(
                bondIssuerMap.getOrDefault(BondType.PUBLIC, List.of()),
                bondIssuerMap.getOrDefault(BondType.BANK, List.of()),
                bondIssuerMap.getOrDefault(BondType.SPECIALIZED_CREDIT, List.of()),
                bondIssuerMap.getOrDefault(BondType.COMPANY, List.of())
        );
    }

    @PostMapping("/api/v1/issuer/{bondIssuerId}/alias")
    public void addBondAlias(@PathVariable("bondIssuerId") Long bondIssuerId, @RequestParam("name") String bondAliasName) {
        bondAliasService.addBondAlias(bondIssuerId, bondAliasName);
    }

    @DeleteMapping("/api/v1/issuer/{bondIssuerId}/alias/{bondAliasId}")
    public void deleteBondAlias(@PathVariable("bondIssuerId") Long bondIssuerId, @PathVariable("bondAliasId") Long bondAliasId) {
        bondAliasService.deleteBondAlias(bondIssuerId, bondAliasId);
    }

}
