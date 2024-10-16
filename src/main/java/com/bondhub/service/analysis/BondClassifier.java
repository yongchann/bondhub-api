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
    private final BondAliasRepository bondAliasRepository;

    protected BondClassifier(BondAliasRepository bondAliasRepository) {
        this.bondAliasRepository = bondAliasRepository;
        this.creditAliasMap = new TreeMap<>( // key length desc
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

    public Optional<BondClassificationResult> extractCreditBondIssuer(String content) {
        for (String alias : creditAliasMap.keySet()) {
            if (content.contains(alias)) {
                BondIssuer bondIssuer = creditAliasMap.get(alias);
                return Optional.of(new BondClassificationResult(bondIssuer, alias));
            }
        }
        return Optional.empty();
    }

}
