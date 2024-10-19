package com.bondhub.domain.aggregation;

import com.bondhub.domain.chat.Chat;
import com.bondhub.domain.chat.MultiBondChat;
import com.bondhub.domain.transaction.Transaction;
import com.bondhub.domain.transaction.TransactionStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Component
public class AnalysisSummaryUpdater {

    private final AnalysisSummaryReader analysisSummaryReader;

    public void updateChatSummary(String date, List<Chat> singleBondChats, List<MultiBondChat> multiBondChats) {
        ChatAggregation aggregation = analysisSummaryReader.findOrCreateChatAggregation(date);
        aggregation.update(singleBondChats, multiBondChats.size());
    }

    public void updateSeparation(String date, List<Chat> separatedChats) {
        ChatAggregation aggregation = analysisSummaryReader.getChatAggregation(date);
        aggregation.updateSeparation(separatedChats);
    }

    public void updateTransactionSummary(String date, List<Transaction> txs) {
        Map<TransactionStatus, Long> statusCounts = txs.stream()
                .collect(Collectors.groupingBy(Transaction::getStatus, Collectors.counting()));

        TransactionAggregation aggregation = analysisSummaryReader.findOrCreateTransactionAggregation(date);
        aggregation.update(statusCounts);
    }

    public void updateRetrialForUncategorized(String date, List<Chat> uncategorizedChats) {
        ChatAggregation aggregation = analysisSummaryReader.getChatAggregation(date);
        aggregation.updateRetrial(uncategorizedChats);
    }
}
