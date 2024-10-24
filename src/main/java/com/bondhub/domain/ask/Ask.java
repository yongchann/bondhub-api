package com.bondhub.domain.ask;

import com.bondhub.service.dto.ChatDto;
import com.bondhub.service.dto.TransactionDetailDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Builder
@Getter
@AllArgsConstructor
public class Ask {

    private String bondName;

    private String maturityDate;

    private String grade;

    private List<ChatDto> chats;

    private List<TransactionDetailDto> transactions;

    public static Ask from(Bond bond) {
        return Ask.builder()
                .bondName(bond.getBondIssuer().getName())
                .maturityDate(bond.getMaturityDate())
                .grade(bond.getBondIssuer().getGrade())
                .chats(new ArrayList<>())
                .transactions(new ArrayList<>())
                .build();
    }

}
