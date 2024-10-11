package com.bondhub.domain.ask;

import com.bondhub.domain.bond.Bond;
import com.bondhub.domain.chat.Chat;
import com.bondhub.domain.transaction.Transaction;
import com.bondhub.service.BondClassifier;
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

    private final BondClassifier bondClassifier;

    public List<Ask> convertToAsk(List<Chat> chats, List<Transaction> transactions) {
        List<String> exclusionKeywords = bondClassifier.getExclusionKeywords();
        Map<Bond, Ask> bondMap = new HashMap<>();

        for (Chat chat : chats) {
            bondMap.computeIfAbsent(chat.getBond(), k -> Ask.from(chat.getBond()))
                    .getChats()
                    .add(ChatDto.builder()
                            .chatId(chat.getId())
                            .senderName(chat.getSenderName())
                            .sendTime(chat.getSendDateTime())
                            .content(chat.getContent())
                            .senderAddress(chat.getSenderAddress())
                            .containExclusionKeyword(exclusionKeywords.stream().anyMatch(keyword -> chat.getContent().contains(keyword)))
                            .build());
        }

        // 거래 내역을 순회하며 해당하는 채권에 추가
        for (Transaction transaction : transactions) {
            Bond transactionBond = transaction.getBond();
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

        // 채팅과 거래 내역 정렬
        for (Ask ask : bondMap.values()) {
            ask.sortChats();
        }

        // 결과를 만기일 기준으로 정렬하여 반환
        return bondMap.values().stream()
                .sorted(Comparator.comparing(Ask::getDueDate))
                .toList();
    }
}
