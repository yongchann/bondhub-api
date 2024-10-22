package com.bondhub.service;

import com.bondhub.domain.ask.AskManager;
import com.bondhub.domain.ask.SimpleAsk;
import com.bondhub.domain.bond.BondType;
import com.bondhub.domain.chat.Chat;
import com.bondhub.domain.chat.ChatFinder;
import com.bondhub.domain.transaction.Transaction;
import com.bondhub.domain.transaction.TransactionFinder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class AskService {

    private final AskManager askManager;
    private final ChatFinder chatFinder;
    private final TransactionFinder transactionFinder;

    public List<SimpleAsk> inquiry(String date, BondType bondType) {
        List<Chat> chats = chatFinder.findDailyLatestSellChats(date, bondType);
        List<Transaction> transactions = transactionFinder.findDailyCreditTransactions(date, bondType);
        return askManager.convertToSimpleAsk(chats, transactions);
    }
}
