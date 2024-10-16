package com.bondhub.service.analysis;

import com.bondhub.domain.bond.BondAlias;
import com.bondhub.domain.bond.BondAliasRepository;
import com.bondhub.domain.bond.BondClassificationResult;
import com.bondhub.domain.bond.BondIssuer;
import com.bondhub.service.event.BondAliasEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

@Slf4j
@Component
public class BondClassifier {

    private SortedMap<String, BondIssuer> aliasToIssuerMap;
    private final BondAliasRepository bondAliasRepository;

    protected BondClassifier(BondAliasRepository bondAliasRepository) {
        this.bondAliasRepository = bondAliasRepository;
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
                bondAliases.forEach(alias -> aliasToIssuerMap.put(alias.getName().toUpperCase(), alias.getBondIssuer()));
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

//    public Bond extractBond(String content, String maturityDate) {
//        for (Map.Entry<String, BondIssuer> entry : aliasToIssuerMap.entrySet()) {
//            String alias = entry.getKey();
//            BondIssuer bondIssuer = entry.getValue();
//            if (content.toUpperCase().contains(alias.toUpperCase())) {
//                return bondRepository.findByBondIssuerAndMaturityDate(bondIssuer, maturityDate)
//                .orElseGet(() -> bondRepository.save(new Bond(bondIssuer, maturityDate)));
//            }
//        }
//        return null;
//    }

    public BondClassificationResult extractBondIssuer(String content) {
        for (String alias : aliasToIssuerMap.keySet()) {
            if (content.contains(alias)) {
                BondIssuer bondIssuer = aliasToIssuerMap.get(alias);
                return new BondClassificationResult(bondIssuer, alias);
            }
        }
        return new BondClassificationResult(null, "");
    }


}
