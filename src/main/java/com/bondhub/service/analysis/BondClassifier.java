package com.bondhub.service.analysis;

import com.bondhub.domain.bond.*;
import com.bondhub.service.event.BondAliasEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.SortedMap;
import java.util.TreeMap;

@Slf4j
@Component
public class BondClassifier {

    private final SortedMap<String, BondIssuer> creditAliasMap;
    private final SortedMap<String, BondType> nonCreditBondTypeMap;
    private final BondAliasRepository bondAliasRepository;

    protected BondClassifier(BondAliasRepository bondAliasRepository) {
        this.bondAliasRepository = bondAliasRepository;
        this.creditAliasMap = new TreeMap<>(BondClassifier::compare);
        this.nonCreditBondTypeMap = new TreeMap<>(BondClassifier::compare);
        nonCreditBondTypeMap.put("ABSTB", BondType.ABSTB);
        nonCreditBondTypeMap.put("국민주택", BondType.KNHB);
        nonCreditBondTypeMap.put("국주", BondType.KNHB);
        nonCreditBondTypeMap.put("ABCP", BondType.ABCP);
        nonCreditBondTypeMap.put("전단채", BondType.STB);
        nonCreditBondTypeMap.put("전단", BondType.STB);
        nonCreditBondTypeMap.put("FRN", BondType.FRN);
        nonCreditBondTypeMap.put("CD+", BondType.FRN);
        nonCreditBondTypeMap.put("CP", BondType.CP);
        nonCreditBondTypeMap.put("CD", BondType.CD);
    }

    private static int compare(String str1, String str2) {
        int lengthComparison = Integer.compare(str2.length(), str1.length());
        if (lengthComparison != 0) {
            return lengthComparison;
        }
        return str1.compareTo(str2);
    }

    @EventListener(BondAliasEvent.class)
    private void handleBondAliasEvent(BondAliasEvent event) {
        log.info(event.getMessage());

        switch (event.getType()) {
            case INITIALIZED:
                creditAliasMap.clear();
                List<BondAlias> bondAliases = bondAliasRepository.findAllFetchBond();
                bondAliases.forEach(alias -> creditAliasMap.put(alias.getName().toUpperCase(), alias.getBondIssuer()));
                break;

            case CREATED:
                BondAlias createdAlias = bondAliasRepository.findByName(event.getBondAliasName()).orElseThrow(
                        () -> new IllegalArgumentException("failed to find new alias: " + event.getBondAliasName())
                );
                creditAliasMap.put(createdAlias.getName(), createdAlias.getBondIssuer());
                break;

            case DELETED:
                creditAliasMap.remove(event.getBondAliasName());
                break;
        }
    }

    public Optional<CreditClassificationResult> extractCreditBondIssuer(String content) {
        String upperCase = content.toUpperCase();

        for (String keyword : creditAliasMap.keySet()) {
            if (upperCase.contains(keyword)) {
                BondIssuer bondIssuer = creditAliasMap.get(keyword);
                return Optional.of(new CreditClassificationResult(bondIssuer, keyword));
            }
        }
        return Optional.empty();
    }

    public Optional<NonCreditClassificationResult> extractNonCreditBondType(String content) {
        String upperCase = content.toUpperCase();

        for (String keyword : nonCreditBondTypeMap.keySet()) {
            if (upperCase.contains(keyword)) {
                if (keyword.equals("국주") && content.contains("한국주택")){
                    return Optional.empty();
                }

                if (keyword.equals("FRN") && content.contains("변고정")){
                    return Optional.empty();
                }

                BondType bondType = nonCreditBondTypeMap.get(keyword);
                return Optional.of(new NonCreditClassificationResult(bondType, keyword));
            }
        }
        return Optional.empty();
    }

}
