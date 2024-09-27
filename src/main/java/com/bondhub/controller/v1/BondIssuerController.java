package com.bondhub.controller.v1;

import com.bondhub.controller.v1.request.AddAliasRequest;
import com.bondhub.controller.v1.request.CreateBondIssuerRequest;
import com.bondhub.controller.v1.request.ModifyBondIssuerRequest;
import com.bondhub.controller.v1.response.AddAliasResponse;
import com.bondhub.controller.v1.response.BondIssuerResponse;
import com.bondhub.domain.bond.BondType;
import com.bondhub.service.BondAliasService;
import com.bondhub.service.BondIssuerService;
import com.bondhub.service.dto.BondAliasDto;
import com.bondhub.service.dto.BondIssuerDto;
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

    @PostMapping("/api/v1/issuer")
    public BondIssuerDto  addBondIssuer(@RequestBody CreateBondIssuerRequest request) {
        return bondIssuerService.create(request.getBondType(), request.getName(), request.getGrade());
    }

    @PostMapping("/api/v1/issuer/{bondIssuerId}/alias")
    public AddAliasResponse addBondAlias(@PathVariable("bondIssuerId") Long bondIssuerId, @RequestBody AddAliasRequest request) {
        BondAliasDto result = bondAliasService.addBondAlias(bondIssuerId, request.getName());
        return new AddAliasResponse(result.getId(), request.getName());
    }

    @PatchMapping("/api/v1/issuer/{bondIssuerId}")
    public void modifyBondIssuer(@PathVariable("bondIssuerId") Long bondIssuerId, @RequestBody ModifyBondIssuerRequest request) {
        bondIssuerService.modify(bondIssuerId, request.getGrade(), request.getName());
    }

    @DeleteMapping("/api/v1/issuer/{bondIssuerId}/alias/{bondAliasId}")
    public void deleteBondAlias(@PathVariable("bondIssuerId") Long bondIssuerId, @PathVariable("bondAliasId") Long bondAliasId) {
        bondAliasService.deleteBondAlias(bondIssuerId, bondAliasId);
    }

}
