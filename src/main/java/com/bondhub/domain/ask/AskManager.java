package com.bondhub.domain.ask;

import com.bondhub.domain.chat.Chat;
import com.bondhub.domain.transaction.Transaction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Component
public class AskManager {

    public List<SimpleAsk> convertToSimpleAsk(List<Chat> chats, List<Transaction> transactions) {
        Map<Bond, Chat> bondChatMap = new HashMap<>();
        Map<Bond, List<Transaction>> bondTransactionMap = new HashMap<>();

        for (Chat chat : chats) {
            Bond bond = new Bond(chat.getBondIssuer(), chat.getMaturityDate());
            Chat sameBondChat = bondChatMap.putIfAbsent(bond, chat);
            if (sameBondChat != null) {
                log.error("[convertToSimpleAsk] 동일 종목 발견 bond_issuer_id={}, maturity_date={}", chat.getBondIssuer().getId(), chat.getMaturityDate());
            }
        }

        for (Transaction tx : transactions) {
            Bond bond = new Bond(tx.getBondIssuer(), tx.getMaturityDate());
            bondTransactionMap.computeIfAbsent(bond, k -> new ArrayList<>()).add(tx);
        }

        List<SimpleAsk> asks = new ArrayList<>();
        for (Map.Entry<Bond, Chat> entry : bondChatMap.entrySet()) {
            Bond bond = entry.getKey();
            Chat bondChat = entry.getValue();
            List<Transaction> bondTransactions = bondTransactionMap.getOrDefault(bond, new ArrayList<>());

            asks.add(SimpleAsk.from(bondChat, bondTransactions));
        }

        return asks;
    }
}
