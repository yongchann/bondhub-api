package com.bbchat.service;

import com.bbchat.domain.bond.Bond;
import com.bbchat.domain.bond.BondAlias;
import com.bbchat.domain.bond.BondIssuer;
import com.bbchat.domain.entity.ExclusionKeyword;
import com.bbchat.repository.BondAliasRepository;
import com.bbchat.repository.BondRepository;
import com.bbchat.repository.ExclusionKeywordRepository;
import com.bbchat.service.event.BondAliasAddedEvent;
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

    @EventListener(BondAliasAddedEvent.class)
    private void load(BondAliasAddedEvent event) {
        log.info(event.getMessage());

        // aliasToIssuerMap
        aliasToIssuerMap.clear();
        List<BondAlias> bondAliases = bondAliasRepository.findAllFetchBond();
        bondAliases.forEach(alias -> aliasToIssuerMap.put(alias.getName(), alias.getBondIssuer()));

        // exclusionKeywords
        exclusionKeywords.clear();
        List<ExclusionKeyword> keywords = this.exclusionKeywordRepository.findAll();
        for (ExclusionKeyword exclusionKeyword : keywords) {
            exclusionKeywords.add(exclusionKeyword.getName());
        }
    }

    public Bond extractBond(String content, String dueDate) {
        for (Map.Entry<String, BondIssuer> entry : aliasToIssuerMap.entrySet()) {
            String alias = entry.getKey();
            BondIssuer bondIssuer = entry.getValue();
            if (content.contains(alias)) {
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
