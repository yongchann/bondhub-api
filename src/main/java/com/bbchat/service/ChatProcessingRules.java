package com.bbchat.service;

import com.bbchat.domain.BondType;
import com.bbchat.domain.entity.Bond;
import com.bbchat.domain.entity.BondAlias;
import com.bbchat.domain.entity.BondIssuer;
import com.bbchat.domain.entity.ExclusionKeyword;
import com.bbchat.repository.BondAliasRepository;
import com.bbchat.repository.BondIssuerRepository;
import com.bbchat.repository.BondRepository;
import com.bbchat.repository.ExclusionKeywordRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.*;

@RequiredArgsConstructor
@Getter
@Component
public class ChatProcessingRules {

    private final ExclusionKeywordRepository exclusionKeywordRepository;
    private final BondAliasRepository bondAliasRepository;
    private final BondRepository bondRepository;
    private final BondIssuerRepository bondIssuerRepository;
    private final ObjectMapper objectMapper;

    private Map<String, BondIssuer> aliasToBondIssuerMap = new HashMap<>();
    private Map<BondIssuer, List<String>> bondAliasesMap = new HashMap<>();
    private Set<Bond> bondSet = new HashSet<>();
    private List<String> exclusionKeywords = new ArrayList<>();
    private List<String> askKeywords = List.of("팔자");
    private Map<String, String> replacementRules = Map.of(
            "[부국채영]368-9532", "([부국채영]368-9532])",
            "김성훈(부국)", "김성훈부국",
            "\r\n", " ");

    @Transactional
    @EventListener(ApplicationReadyEvent.class)
    protected void init() {
        initBondIssuers();
        initExclusionKeywords();
        refresh();
    }

    private void initBondIssuers() {
        loadBondsFromFile("bond_bank.json", BondType.BANK);
        loadBondsFromFile("bond_company.json", BondType.COMPANY);
        loadBondsFromFile("bond_public.json", BondType.PUBLIC);
        loadBondsFromFile("bond_specialized_credit.json", BondType.SPECIALIZED_CREDIT);

        for (Map.Entry<BondIssuer, List<String>> entry : bondAliasesMap.entrySet()) {
            List<String> aliases = entry.getValue();
            aliases.sort((a, b) -> Integer.compare(b.length(), a.length())); // 길이 역순으로 정렬
            bondAliasesMap.put(entry.getKey(), aliases); // 정렬된 리스트로 다시 저장
        }
    }

    private void loadBondsFromFile(String fileName, BondType bondType) {
        ClassPathResource resource = new ClassPathResource(fileName);
        try {
            Map<String, List<String>> bondIssuerWithAliases = objectMapper.readValue(resource.getInputStream(), new TypeReference<>() {});
            bondIssuerWithAliases.forEach((issuerName, aliases) -> {
                BondIssuer bondIssuer = BondIssuer.builder()
                        .type(bondType)
                        .name(issuerName)
                        .build();
                bondIssuerRepository.save(bondIssuer);

                List<BondAlias> bondAliases = aliases.stream().map(alias -> BondAlias.builder()
                        .bondIssuer(bondIssuer)
                        .name(alias)
                        .build()).toList();

                bondAliasRepository.saveAll(bondAliases);

                bondAliasesMap.put(bondIssuer, aliases);
            });
        } catch (IOException e) {
            throw new RuntimeException("Error loading bonds from file: " + fileName, e);
        }
    }

    private void initExclusionKeywords() {
        String fileName = "exclusion_keyword.json";
        ClassPathResource resource = new ClassPathResource(fileName);
        try {
            List<String> exclusionKeywords = objectMapper.readValue(resource.getInputStream(), new TypeReference<>() {});
            exclusionKeywords.forEach(keyword -> {
                ExclusionKeyword exclusionKeyword = ExclusionKeyword.builder()
                        .name(keyword).build();
                exclusionKeywordRepository.save(exclusionKeyword);
            });
        } catch (IOException e) {
            throw new RuntimeException("Error loading bonds from file: " + fileName, e);
        }
    }

    public void refresh() {
        aliasToBondIssuerMap.clear();
        List<BondAlias> bondAliases = bondAliasRepository.findAllFetchBond();
        for (BondAlias bondAlias : bondAliases) {
            BondIssuer bondIssuer = bondAlias.getBondIssuer();
            aliasToBondIssuerMap.put(bondAlias.getName(), bondIssuer);
        }

        exclusionKeywords.clear();
        List<ExclusionKeyword> exclusionKeywordList = exclusionKeywordRepository.findAll();
        for (ExclusionKeyword exclusionKeyword : exclusionKeywordList) {
            exclusionKeywords.add(exclusionKeyword.getName());
        }
    }

    public void updateBondSet(Set<Bond> newBondSet) {
        this.bondSet.addAll(newBondSet);
    }
}
