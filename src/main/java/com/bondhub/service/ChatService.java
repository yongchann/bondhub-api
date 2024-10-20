package com.bondhub.service;

import com.bondhub.domain.aggregation.AnalysisSummaryUpdater;
import com.bondhub.domain.chat.*;
import com.bondhub.service.analysis.ChatAnalyzer;
import com.bondhub.service.analysis.MaturityDateExtractor;
import com.bondhub.service.dto.ChatDto;
import com.bondhub.service.dto.ChatGroupByContentDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class ChatService {

    private final ChatFinder chatFinder;
    private final ChatAppender chatAppender;
    private final ChatAnalyzer chatAnalyzer;

    private final MultiBondChatFinder multiBondChatFinder;
    private final MultiBondChatAppender multiBondChatAppender;
    private final MultiBondChatProcessor multiBondChatProcessor;

    private final MaturityDateExtractor maturityDateExtractor;

    private final AnalysisSummaryUpdater analysisSummaryUpdater;

    @Transactional
    public void append(String date, List<ChatDto> chats) {
        List<Chat> singleBondChats = new ArrayList<>();
        List<MultiBondChat> multiBondChats = new ArrayList<>();

        for (ChatDto chat : chats) {
            List<String> maturities = maturityDateExtractor.extractAllMaturities(chat.getContent());
            if (maturities.size() >= 2) {
                multiBondChatFinder.find(date, chat.getSenderName(), chat.getContent()).ifPresentOrElse(
                        result -> result.updateChatDateTime(chat.getChatDateTime()),
                        () -> multiBondChats.add(ChatDto.toMultiBondChatEntity(chat, maturities)));
            }
            else {
                // TODO 채팅 유일성 검사
                Chat chatEntity = ChatDto.toChatEntity(chat, maturities);
                chatAnalyzer.analyze(chatEntity);
                singleBondChats.add(chatEntity);
            }
        }

        chatAppender.appendInBatch(singleBondChats);
        multiBondChatAppender.append(multiBondChats);

        analysisSummaryUpdater.updateChatSummary(date, singleBondChats, multiBondChats);
    }

    @Transactional
    public int split(String chatDate, Long multiBondChatId, List<String> singleBondContents) {
        MultiBondChat multiBondChat = multiBondChatFinder.getById(multiBondChatId);

        // 복수 종목 호가를 n 개로 분리
        List<Chat> separatedChats = multiBondChatProcessor.separate(multiBondChat, singleBondContents);

        // 분리된 채팅 분석
        separatedChats.forEach(chatAnalyzer::analyze);

        chatAppender.append(separatedChats);

        analysisSummaryUpdater.updateSeparation(chatDate, separatedChats);

        return separatedChats.size();
    }

    @Transactional
    public void discardChats(String chatDate, List<Long> chatIds) {
        List<Chat> targetChats = chatFinder.findDailyInIds(chatDate, chatIds);
        if (chatIds.size() != targetChats.size()) {
            throw new IllegalArgumentException("Mismatch between requested and retrieved chat count.");
        }

        targetChats.forEach(Chat::discard);
    }

    public List<ChatGroupByContentDto> findChatsGroupByContent(String chatDate, ChatStatus status) {
        return chatFinder.findDailyByStatusGroupByContent(chatDate, status);
    }

    @Transactional
    public void retryForUncategorizedChat(String chatDate) {
        List<Chat> uncategorizedChats = chatFinder.findDailyByStatus(chatDate, ChatStatus.UNCATEGORIZED);

        // 재분류
        uncategorizedChats.forEach(chatAnalyzer::analyze);

        analysisSummaryUpdater.updateRetrialForUncategorized(chatDate, uncategorizedChats);
    }


    @Transactional
    public void autoSplit(String date, int limit) {
        // 대상 조회
        List<MultiBondChat> multiBondChats = multiBondChatFinder.findDailyByStatus(date, ChatStatus.NEEDS_SEPARATION, limit);

        // 자동 분리
        List<Chat> separatedChats = multiBondChatProcessor.autoSeparate(multiBondChats);

        // 분리된 채팅 분석
        separatedChats.forEach(chatAnalyzer::analyze);

        chatAppender.appendInBatch(separatedChats);

        analysisSummaryUpdater.updateSeparation(date, separatedChats);
    }
}
