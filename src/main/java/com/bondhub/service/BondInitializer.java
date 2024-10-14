package com.bondhub.service;

import com.bondhub.domain.bond.*;
import com.bondhub.service.dto.BondIssuerJson;
import com.bondhub.service.event.BondAliasEvent;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Component
public class BondInitializer {

    private final ObjectMapper objectMapper;
    private final BondIssuerRepository bondIssuerRepository;
    private final BondAliasRepository bondAliasRepository;

    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    @EventListener(ApplicationReadyEvent.class)
    protected void init() {
        if (isRequiredDbSetup()){
            dbSetupFromJsonFile();
        }

        eventPublisher.publishEvent(new BondAliasEvent(this, BondAliasEvent.Type.INITIALIZED, "BondInitializer succeeded to update bond info", ""));
    }

    private void dbSetupFromJsonFile() {
        Map<String, BondType> fileNameBondTypeMap = Map.of(
                "PUBLIC.json", BondType.PUBLIC,
                "COMMERCIAL_BANK.json", BondType.COMMERCIAL_BANK,
                "SPECIAL_BANK.json", BondType.SPECIAL_BANK,
                "CARD.json", BondType.CARD,
                "CAPITAL.json", BondType.CAPITAL,
                "COMPANY.json", BondType.COMPANY
        );

        fileNameBondTypeMap.forEach((fileName, bondType) -> {
            try {
                Map<String, BondIssuerJson> bondAliasMap = loadBondAliasFromJson(fileName);
                bondAliasMap.forEach((issuerName, issuerData) -> {
                    BondIssuer bondIssuer = BondIssuer.builder()
                            .type(bondType)
                            .grade(issuerData.getGrade() != null ? issuerData.getGrade() : "")
                            .name(issuerName)
                            .build();
                    bondIssuerRepository.save(bondIssuer);

                    List<BondAlias> bondAliases = issuerData.getAliases().stream().map(alias -> BondAlias.builder()
                            .bondIssuer(bondIssuer)
                            .name(alias)
                            .build()).toList();
                    bondAliasRepository.saveAll(bondAliases);
                });
                log.info("registered bond issuer and alias of %s".formatted(bondType.name()));

            } catch (IOException e) {
                log.error("failed to register bond issuer and alias of %s".formatted(bondType.name()));
                throw new RuntimeException(e);
            }
        });
    }

    private boolean isRequiredDbSetup() {
        return bondIssuerRepository.count() < 1;
    }

    private Map<String, BondIssuerJson> loadBondAliasFromJson(String filePath) throws IOException {
        ClassPathResource resource = new ClassPathResource(filePath);
        return objectMapper.readValue(resource.getInputStream(), new TypeReference<>() {});
    }

}
