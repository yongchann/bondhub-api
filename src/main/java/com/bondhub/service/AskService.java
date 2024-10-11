package com.bondhub.service;

import com.bondhub.domain.ask.Ask;
import com.bondhub.domain.ask.AskManager;
import com.bondhub.domain.bond.BondType;
import com.bondhub.domain.chat.Chat;
import com.bondhub.domain.chat.ChatReader;
import com.bondhub.domain.transaction.Transaction;
import com.bondhub.domain.transaction.TxReader;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
public class AskService {

    private final AskManager askManager;
    private final ChatReader chatReader;
    private final ChatProcessor chatProcessor;
    private final TxReader txReader;

    public List<Ask> inquiry(String date, BondType bondType) {
        List<Chat> chats = chatReader.getClassifiedChat(date, bondType);
        ArrayList<Chat> uniqueChats = chatProcessor.removeDuplication(chats);

        List<Transaction> transactions = txReader.getClassifiedTx(date, bondType);

        return askManager.convertToAsk(uniqueChats, transactions);
    }
}
