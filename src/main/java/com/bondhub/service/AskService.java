package com.bondhub.service;

import com.bondhub.domain.ask.Ask;
import com.bondhub.domain.ask.AskManager;
import com.bondhub.domain.bond.BondType;
import com.bondhub.domain.chat.Chat;
import com.bondhub.domain.chat.ChatFinder;
import com.bondhub.domain.chat.ChatProcessor;
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
    private final ChatProcessor chatProcessor;
    private final TransactionFinder transactionFinder;

    public List<Ask> inquiry(String date, BondType bondType) {
        List<Chat> chats = chatFinder.findDailyCreditSellChats(date, bondType);
        List<Chat> uniqueChats = chatProcessor.removeDuplication(chats);
        List<Transaction> transactions = transactionFinder.findDailyCreditTransactions(date, bondType);
        return askManager.convertToAsk(uniqueChats, transactions);
    }
}
