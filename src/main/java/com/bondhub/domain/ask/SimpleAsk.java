package com.bondhub.domain.ask;

import com.bondhub.domain.chat.Chat;
import com.bondhub.domain.transaction.Transaction;
import com.bondhub.service.dto.TransactionDetailDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
@AllArgsConstructor
public class SimpleAsk {

    private String bondName;

    private String maturityDate;

    private String grade;

    private Chat lastChat;

    private List<TransactionDetailDto> transactions;

    public static SimpleAsk from(Chat chat, List<Transaction> txs) {
        return SimpleAsk.builder()
                .bondName(chat.getBondIssuer().getName())
                .maturityDate(chat.getMaturityDate())
                .grade(chat.getBondIssuer().getGrade())
                .lastChat(chat)
                .transactions(txs.stream().map(TransactionDetailDto::from).toList())
                .build();
    }

}
