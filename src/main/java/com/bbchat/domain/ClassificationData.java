package com.bbchat.domain;

import com.bbchat.domain.entity.Bond;
import com.bbchat.domain.entity.BondAlias;
import com.bbchat.domain.entity.ExclusionKeyword;
import com.bbchat.domain.repository.BondAliasRepository;
import com.bbchat.domain.repository.ExclusionKeywordRepository;
import lombok.Getter;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
@Component
public class ClassificationData {

    private final ExclusionKeywordRepository exclusionKeywordRepository;
    private final BondAliasRepository bondAliasRepository;
    private Map<String, Bond> bondAliasMap;
    private List<String> exclusionKeywords;

    public ClassificationData(ExclusionKeywordRepository exclusionKeywordRepository, BondAliasRepository bondAliasRepository) {
        this.exclusionKeywordRepository = exclusionKeywordRepository;
        this.bondAliasRepository = bondAliasRepository;

        List<BondAlias> bondAliases = bondAliasRepository.findAllFetchBond();
        Map<String, Bond> bondMap = bondAliases.stream().collect(Collectors.toMap(BondAlias::getName, BondAlias::getBond));
        this.bondAliasMap = bondMap.entrySet()
                .stream()
                .sorted((e1, e2) -> Integer.compare(e2.getKey().length(), e1.getKey().length()))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));

        List<ExclusionKeyword> exclusionKeywords = exclusionKeywordRepository.findAll();
        this.exclusionKeywords = exclusionKeywords.stream().map(ExclusionKeyword::getName).toList();
    }

}
