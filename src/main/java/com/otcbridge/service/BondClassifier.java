package com.otcbridge.service;

import com.otcbridge.domain.bond.Bond;
import com.otcbridge.domain.bond.BondAlias;
import com.otcbridge.domain.bond.BondIssuer;
import com.otcbridge.domain.chat.ExclusionKeyword;
import com.otcbridge.repository.BondAliasRepository;
import com.otcbridge.repository.BondRepository;
import com.otcbridge.repository.ExclusionKeywordRepository;
import com.otcbridge.service.event.BondAliasEvent;
import com.otcbridge.service.event.ExclusionKeywordEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.*;

@Slf4j
@Component
public class BondClassifier {

    private SortedMap<String, BondIssuer> aliasToIssuerMap;
    private List<String> exclusionKeywords = new ArrayList<>();

    private final ExclusionKeywordRepository exclusionKeywordRepository;
    private final BondAliasRepository bondAliasRepository;
    private final BondRepository bondRepository;

    protected BondClassifier(ExclusionKeywordRepository exclusionKeywordRepository, BondAliasRepository bondAliasRepository, BondRepository bondRepository) {
        this.exclusionKeywordRepository = exclusionKeywordRepository;
        this.bondAliasRepository = bondAliasRepository;
        this.bondRepository = bondRepository;
        this.aliasToIssuerMap = new TreeMap<>( // key length desc
                (str1, str2) -> {
                    int lengthComparison = Integer.compare(str2.length(), str1.length());
                    if (lengthComparison != 0) {
                        return lengthComparison;
                    }
                    return str1.compareTo(str2);
                }
        );
    }

    @EventListener(BondAliasEvent.class)
    private void handleBondAliasEvent(BondAliasEvent event) {
        log.info(event.getMessage());

        switch (event.getType()) {
            case INITIALIZED:
                aliasToIssuerMap.clear();
                List<BondAlias> bondAliases = bondAliasRepository.findAllFetchBond();
                bondAliases.forEach(alias -> aliasToIssuerMap.put(alias.getName(), alias.getBondIssuer()));
                break;

            case CREATED:
                BondAlias createdAlias = bondAliasRepository.findByName(event.getBondAliasName()).orElseThrow(
                        () -> new IllegalArgumentException("failed to find new alias: " + event.getBondAliasName())
                );
                aliasToIssuerMap.put(createdAlias.getName(), createdAlias.getBondIssuer());
                break;

            case DELETED:
                aliasToIssuerMap.remove(event.getBondAliasName());
                break;
        }
    }

    @EventListener(ExclusionKeywordEvent.class)
    public void handleExclusionKeyword(ExclusionKeywordEvent event) {
        log.info("ExclusionKeyword {}, {}",event.getType().name(), event.getKeyword());

        switch (event.getType()) {
            case INITIALIZED:
                exclusionKeywords.clear();
                List<ExclusionKeyword> keywords = this.exclusionKeywordRepository.findAll();
                for (ExclusionKeyword exclusionKeyword : keywords) {
                    exclusionKeywords.add(exclusionKeyword.getName());
                }
                break;

            case CREATED:
                ExclusionKeyword createdKeyword = exclusionKeywordRepository.findByName(event.getKeyword()).orElseThrow(
                        () -> new IllegalArgumentException("failed to find new keyword: " + event.getKeyword()));
                exclusionKeywords.add(createdKeyword.getName());
                break;

            case DELETED:
                exclusionKeywords.remove(event.getKeyword());
                break;

        }

    }

    public Bond extractBond(String content, String dueDate) {
        for (Map.Entry<String, BondIssuer> entry : aliasToIssuerMap.entrySet()) {
            String alias = entry.getKey();
            BondIssuer bondIssuer = entry.getValue();
            if (content.toUpperCase().contains(alias.toUpperCase())) {
                return bondRepository.findByBondIssuerAndDueDate(bondIssuer, dueDate)
                .orElseGet(() -> bondRepository.save(new Bond(bondIssuer, dueDate)));
            }
        }
        return null;
    }

    public List<String> getExclusionKeywords() {
        return exclusionKeywords;
    }

}
