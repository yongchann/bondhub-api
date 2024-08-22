package com.bbchat.service;

import com.bbchat.domain.BondType;
import com.bbchat.domain.entity.Bond;
import com.bbchat.domain.entity.BondAlias;
import com.bbchat.domain.entity.ExclusionKeyword;
import com.bbchat.repository.BondAliasRepository;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Getter
@Component
public class ChatProcessingRules {

    private final ExclusionKeywordRepository exclusionKeywordRepository;
    private final BondAliasRepository bondAliasRepository;
    private final BondRepository bondRepository;
    private final ObjectMapper objectMapper;

    private Map<String, Bond> aliasToBondMap = new HashMap<>();
    private List<String> exclusionKeywords = new ArrayList<>();
    private List<String> askKeywords = List.of("팔자");
    private Map<String, String> replacementRules = Map.of(
            "[부국채영]368-9532", "([부국채영]368-9532])",
            "김성훈(부국)", "김성훈부국",
            "\r\n", " ");

    @Transactional
    @EventListener(ApplicationReadyEvent.class)
    protected void init() {
        initBonds();
        initExclusionKeywords();
        refresh();
    }

    private void initBonds() {
        loadBondsFromFile("bond_bank.json", BondType.BANK);
        loadBondsFromFile("bond_company.json", BondType.COMP);
        loadBondsFromFile("bond_public.json", BondType.PUB);
        loadBondsFromFile("bond_specialized_credit.json", BondType.FIN);
    }

    private void loadBondsFromFile(String fileName, BondType bondType) {
        ClassPathResource resource = new ClassPathResource(fileName);
        try {
            Map<String, List<String>> bondMap = objectMapper.readValue(resource.getInputStream(), new TypeReference<>() {});
            bondMap.forEach((name, aliases) -> {
                Bond bond = bondRepository.save(Bond.builder()
                        .type(bondType)
                        .primaryName(name)
                        .build());

                aliases.forEach(alias -> bondAliasRepository.save(BondAlias.builder()
                        .bond(bond)
                        .name(alias)
                        .build()));
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
        aliasToBondMap.clear();
        List<BondAlias> bondAliases = bondAliasRepository.findAllFetchBond();
        for (BondAlias bondAlias : bondAliases) {
            Bond bond = bondAlias.getBond();
            aliasToBondMap.put(bondAlias.getName(), bond);
        }

        exclusionKeywords.clear();
        List<ExclusionKeyword> exclusionKeywordList = exclusionKeywordRepository.findAll();
        for (ExclusionKeyword exclusionKeyword : exclusionKeywordList) {
            exclusionKeywords.add(exclusionKeyword.getName());
        }
    }


}
