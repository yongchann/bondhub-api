package com.bondhub.service.dto;

import com.bondhub.domain.ask.Bond;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.*;


@Builder
@AllArgsConstructor
@Getter
public class BondChatDto {

    private String bondName;

    private String maturityDate;

    private String grade;

    private List<ChatDto> chats;

    public static BondChatDto from(Bond bond) {
        return BondChatDto.builder()
                .bondName(bond.getBondIssuer().getName())
                .maturityDate(bond.getMaturityDate())
                .grade(bond.getBondIssuer().getGrade())
                .chats(new ArrayList<>())
                .build();
    }

}
