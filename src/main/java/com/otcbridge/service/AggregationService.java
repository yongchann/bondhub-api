package com.otcbridge.service;

import com.otcbridge.domain.aggregation.ChatAggregation;
import com.otcbridge.domain.aggregation.ChatAggregationResult;
import com.otcbridge.domain.aggregation.TransactionAggregation;
import com.otcbridge.domain.aggregation.TransactionAggregationResult;
import com.otcbridge.domain.chat.Chat;
import com.otcbridge.domain.chat.ChatStatus;
import com.otcbridge.domain.transaction.Transaction;
import com.otcbridge.domain.transaction.TransactionStatus;
import com.otcbridge.repository.ChatAggregationRepository;
import com.otcbridge.repository.ChatRepository;
import com.otcbridge.repository.TransactionRepository;
import com.otcbridge.repository.TransactionAggregationRepository;
import com.otcbridge.service.exception.NotFoundAggregationException;
import com.otcbridge.support.FileInfo;
import com.otcbridge.support.S3FileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.otcbridge.service.UploadService.CHAT_FILE_KEY_PREFIX;
import static com.otcbridge.service.UploadService.CHAT_FILE_SAVE_NAME;
import static com.otcbridge.support.S3FileRepository.buildPath;

@Slf4j
@RequiredArgsConstructor
@Service
public class AggregationService {

    private final S3FileRepository fileRepository;

    private final ChatRepository chatRepository;
    private final ChatProcessor chatProcessor;
    private final ChatAggregationRepository chatAggregationRepository;

    private final TransactionRepository transactionRepository;
    private final TransactionService transactionService;
    private final TransactionProcessor transactionProcessor;
    private final TransactionAggregationRepository transactionAggregationRepository;

    @Transactional
    public void aggregateChat(String date) {
        // 홰당 일자의 채팅을 모두 삭제
        int deletedCount = chatRepository.deleteAllByChatDateInBatch(date);
        log.info("[aggregateChat] deleted {} chats", deletedCount);

        // 3개의 채팅 데이터 조회 후 하나의 문자열로 병합
        String entireChatStr = Stream.of("BB", "RB", "MM")
                .map(type -> {
                    FileInfo chatFile = fileRepository.get(buildPath(CHAT_FILE_KEY_PREFIX, date, type), CHAT_FILE_SAVE_NAME);
                    return chatProcessor.preprocess(chatFile.getContent());
                })
                .collect(Collectors.joining());

        // 채팅 가공 후 모두 저장
        List<Chat> allChats = chatProcessor.processChatStr(date, entireChatStr);
        chatRepository.saveAll(allChats); // TODO JDBC Batch insert 적용
        log.info("[aggregateChat] created {} chats", allChats.size());

        // 채팅 가공 상태에 따른 집계 결과를 생성
        Map<ChatStatus, Long> statusCounts = allChats.stream()
                .collect(Collectors.groupingBy(Chat::getStatus, Collectors.counting()));

        ChatAggregation aggregation = ChatAggregation.builder()
                .chatDate(date)
                .totalChatCount(allChats.size())
                .notUsedChatCount(statusCounts.getOrDefault(ChatStatus.CREATED, 0L))
                .multiDueDateChatCount(statusCounts.getOrDefault(ChatStatus.MULTI_DD, 0L))
                .uncategorizedChatCount(statusCounts.getOrDefault(ChatStatus.UNCATEGORIZED, 0L))
                .fullyProcessedChatCount(statusCounts.getOrDefault(ChatStatus.OK, 0L))
                .build();

        chatAggregationRepository.save(aggregation);
    }

    @Transactional
    public void aggregateTransaction(String date) {
        // 해당 일자의 거래내역을 모두 삭제
        int deletedCount = transactionRepository.deleteAllByTransactionDateInBatch(date);
        log.info("[aggregateTransaction] deleted {} transactions", deletedCount);

        InputStream inputStream = transactionService.findTransactionFileContent(date);

        // 집계
        List<Transaction> allTx = transactionProcessor.processTransactionFileInputStream(date, inputStream);
        transactionRepository.saveAll(allTx);
        log.info("[aggregateTransaction] created {} transactions", allTx.size());

        Map<TransactionStatus, Long> statusCounts = allTx.stream()
                .collect(Collectors.groupingBy(Transaction::getStatus, Collectors.counting()));

        // 집계 결과 생성
        TransactionAggregation aggregation = TransactionAggregation.builder()
                .transactionDate(date)
                .totalTransactionCount(allTx.size())
                .excludedTransactionCount(statusCounts.getOrDefault(TransactionStatus.NOT_USED, 0L))
                .uncategorizedTransactionCount(statusCounts.getOrDefault(TransactionStatus.UNCATEGORIZED, 0L))
                .fullyProcessedTransactionCount(statusCounts.getOrDefault(TransactionStatus.OK, 0L))
                .build();

        transactionAggregationRepository.save(aggregation);
    }

    public ChatAggregationResult getChatAggregation(String date) {
        ChatAggregation aggregation = chatAggregationRepository.findTopByChatDateOrderByCreatedDateDesc(date)
                .orElseThrow(() -> new NotFoundAggregationException("not found chat aggregation of " + date));

        return ChatAggregationResult.from(aggregation);
    }

    public List<ChatAggregationResult> getChatAggregationHistory(String date) {
        List<ChatAggregation> aggregations = chatAggregationRepository.findByChatDateOrderByCreatedDateDesc(date);
        return aggregations.stream().map(ChatAggregationResult::from).toList();
    }

    public TransactionAggregationResult getTransactionAggregation(String date) {
        TransactionAggregation aggregation = transactionAggregationRepository.findTopByTransactionDateOrderByCreatedDateDesc(date)
                .orElseThrow(() -> new NotFoundAggregationException("not found chat aggregation of " + date));

        return TransactionAggregationResult.from(aggregation);
    }

}
