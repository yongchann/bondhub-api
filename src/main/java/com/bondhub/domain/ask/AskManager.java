package com.bondhub.domain.ask;

import com.bondhub.domain.chat.Chat;
import com.bondhub.domain.transaction.Transaction;
import com.bondhub.service.dto.ChatDto;
import com.bondhub.service.dto.TransactionDetailDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

@Slf4j
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
