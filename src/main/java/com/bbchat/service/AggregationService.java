package com.bbchat.service;

import com.bbchat.domain.aggregation.ChatAggregation;
import com.bbchat.domain.aggregation.ChatAggregationResult;
import com.bbchat.domain.aggregation.TransactionAggregation;
import com.bbchat.domain.aggregation.TransactionAggregationResult;
import com.bbchat.domain.chat.Chat;
import com.bbchat.domain.chat.ChatStatus;
import com.bbchat.repository.ChatAggregationRepository;
import com.bbchat.repository.ChatRepository;
import com.bbchat.repository.TransactionAggregationRepository;
import com.bbchat.service.exception.NotFoundAggregationException;
import com.bbchat.support.FileInfo;
import com.bbchat.support.S3FileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.bbchat.service.UploadService.CHAT_FILE_KEY_PREFIX;
import static com.bbchat.service.UploadService.CHAT_FILE_SAVE_NAME;
import static com.bbchat.support.S3FileRepository.buildPath;

@Slf4j
@RequiredArgsConstructor
@Service
public class AggregationService {

    private final S3FileRepository fileRepository;

    private final ChatRepository chatRepository;
    private final ChatProcessor chatProcessor;
    private final ChatAggregationRepository chatAggregationRepository;

    private final DailyTransactionService transactionService;
    private final TransactionProcessor transactionProcessor;
    private final TransactionAggregationRepository transactionAggregationRepository;

    @Transactional
    public void aggregateAll(String date) {
        // 홰당 일자의 채팅을 모두 삭제
        int deletedCount = chatRepository.deleteAllByChatDateInBatch(date);
        log.info("[aggregateAll] deleted {} chats", deletedCount);

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
        log.info("[aggregateAll] created {} chats", allChats.size());

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
        InputStream inputStream = transactionService.findTransactionFileContent(date);

        // 집계
        TransactionAggregationResult result = transactionProcessor.aggregateFromInputStream(date, inputStream);

        // 집계 결과를 토대로 TransactionAggregation 생성
        TransactionAggregation aggregation = TransactionAggregation.builder()
                .transactionDate(date)
                .result(result)
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
        TransactionAggregation aggregation = transactionAggregationRepository.findByTransactionDate(date)
                .orElseThrow(() -> new NotFoundAggregationException("not found chat aggregation of " + date));

        return TransactionAggregationResult.from(aggregation);
    }

}
