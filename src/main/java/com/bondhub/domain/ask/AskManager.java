package com.bondhub.domain.ask;

import com.bondhub.domain.chat.Chat;
import com.bondhub.domain.transaction.Transaction;
import com.bondhub.service.dto.ChatDto;
import com.bondhub.service.dto.TransactionDetailDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Component
public class AskManager {

    public List<Ask> convertToAsk(List<Chat> chats, List<Transaction> transactions) {
        Map<Bond, Ask> bondMap = new HashMap<>();

        for (Chat chat : chats) {
            Bond bond = new Bond(chat.getBondIssuer(), chat.getMaturityDate());
            bondMap.computeIfAbsent(bond, k -> Ask.from(bond))
                    .getChats()
                    .add(ChatDto.builder()
                            .chatId(chat.getId())
                            .senderName(chat.getSenderName())
                            .chatDateTime(chat.getChatDateTime())
                            .content(chat.getContent())
                            .senderAddress(chat.getSenderAddress())
                            .build());
        }

        // 거래 내역을 순회하며 해당하는 채권에 추가
        for (Transaction transaction : transactions) {
            Bond transactionBond = new Bond(transaction.getBondIssuer(), transaction.getMaturityDate());
            if (bondMap.containsKey(transactionBond)) {
                bondMap.get(transactionBond)
                        .getTransactions()
                        .add(new TransactionDetailDto(
                                transaction.getTime(),
                                transaction.getYield(),
                                transaction.getTradingYield(),
                                transaction.getSpreadBp()
                        ));
            }
            // 채팅이 없는 채권의 거래는 무시 (continue)
        }

        // 결과를 만기일 기준으로 정렬하여 반환
        return bondMap.values().stream()
                .sorted(Comparator.comparing(Ask::getMaturityDate))
                .toList();
    }
}
