package com.otcbridge.controller.v1;

import com.otcbridge.controller.v1.request.*;
import com.otcbridge.controller.v1.response.ExclusionKeywordResponse;
import com.otcbridge.service.ChatService;
import com.otcbridge.service.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
public class ChatController {

    private final ChatService chatService;

    @PostMapping("/api/v1/chat/recent")
    public void appendRecentChats(@RequestBody AppendRecentChatRequest request) {
        List<ChatDto> recentChats = request.getRecentChats().stream()
                .map(chat -> ChatDto.builder()
                        .chatDate(request.getChatDate())
                        .senderName(chat.getSenderName())
                        .sendTime(chat.getSendTime())
                        .content(chat.getContent())
                        .senderAddress(chat.getSenderAddress())
                        .build())
                .toList();

        chatService.append(request.getChatDate(), recentChats);
    }

    @GetMapping("/api/v1/chat/uncategorized")
    public List<UncategorizedChatDto> findUncategorizedChats(@RequestParam("date") String date) {
        return chatService.findUncategorizedChats(date);
    }

    @PostMapping("/api/v1/chat/uncategorized/retry")
    public List<BondChatDto> retryAggregation(@RequestBody RetryForUncategorizedChatRequest request) {
        return chatService.retryForUncategorizedChat(request.getDate());
    }

    @GetMapping("/api/v1/chat/multi-bond")
    public List<MultiBondChatDto> findMultiBondChats(@RequestParam("date") String date) {
        return chatService.findMultiBondChats(date);
    }

    @PostMapping("/api/v1/chat/multi-bond/split")
    public int splitMultiBondChat(@RequestBody SplitMultiBondChatRequest request) {
        return chatService.split(request.getChatDate(), request.getIds(), request.getOriginalContent(), request.getSplitContents());
    }

    @PatchMapping("/api/v1/chat/discard")
    public void discardChats(@RequestBody DiscardChatsRequest request) {
        chatService.discardChats(request.getChatDate(), request.getStatus(), request.getChatIds());
    }

    @GetMapping("/api/v1/chat/exclusion-keyword")
    public ExclusionKeywordResponse getExclusionKeywords() {
        List<ExclusionKeywordDto> exclusionKeywords = chatService.getExclusionKeywords();
        return new ExclusionKeywordResponse(exclusionKeywords);
    }

    @DeleteMapping("/api/v1/chat/exclusion-keyword/{id}")
    public void deleteExclusionKeywords(@PathVariable(name = "id") Long exclusionKeywordId) {
        chatService.deleteExclusionKeywords(exclusionKeywordId);
    }

    @PostMapping("/api/v1/chat/exclusion-keyword")
    public String createExclusionKeyword(@RequestBody CreateExclusionKeywordRequest request) {
        return chatService.createExclusionKeyword(request.getName());
    }
}
