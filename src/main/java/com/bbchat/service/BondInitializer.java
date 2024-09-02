package com.bbchat.service;

import com.bbchat.domain.bond.BondAlias;
import com.bbchat.domain.bond.BondIssuer;
import com.bbchat.domain.bond.BondType;
import com.bbchat.domain.chat.ExclusionKeyword;
import com.bbchat.repository.BondAliasRepository;
import com.bbchat.repository.BondIssuerRepository;
import com.bbchat.repository.ExclusionKeywordRepository;
import com.bbchat.service.dto.BondIssuerJson;
import com.bbchat.service.event.BondAliasEvent;
import com.bbchat.service.event.ExclusionKeywordEvent;
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
    private final ExclusionKeywordRepository exclusionKeywordRepository;

    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    @EventListener(ApplicationReadyEvent.class)
    protected void init() {
        if (isRequiredDbSetup()){
            dbSetupFromJsonFile();
        }

        eventPublisher.publishEvent(new BondAliasEvent(this, BondAliasEvent.Type.INITIALIZED, "BondInitializer succeeded to update bond info", ""));
        eventPublisher.publishEvent(new ExclusionKeywordEvent(this, ExclusionKeywordEvent.Type.INITIALIZED,"BondInitializer succeeded to update exclusion keywords", ""));
    }

    private void dbSetupFromJsonFile() {
        Map<String, BondType> fileNameBondTypeMap = Map.of(
                "bond_bank.json", BondType.BANK, "bond_public.json", BondType.PUBLIC,
                "bond_company.json", BondType.COMPANY, "bond_specialized_credit.json", BondType.SPECIALIZED_CREDIT);

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

        try {
            List<String> keywordsJson = loadExclusionKeywordFromJson("exclusion_keyword.json");
            List<ExclusionKeyword> exclusionKeywords = keywordsJson.stream().map(ExclusionKeyword::new).toList();
            exclusionKeywordRepository.saveAll(exclusionKeywords);
        } catch (IOException e) {
            log.error("failed to register exclusion keywords");
            throw new RuntimeException(e);
        }
    }

    private boolean isRequiredDbSetup() {
        return bondIssuerRepository.count() < 1;
    }

    private Map<String, BondIssuerJson> loadBondAliasFromJson(String filePath) throws IOException {
        ClassPathResource resource = new ClassPathResource(filePath);
        return objectMapper.readValue(resource.getInputStream(), new TypeReference<>() {});
    }

    private List<String> loadExclusionKeywordFromJson(String filePath) throws IOException {
        ClassPathResource resource = new ClassPathResource(filePath);
        return objectMapper.readValue(resource.getInputStream(), new TypeReference<>() {});
    }
}
