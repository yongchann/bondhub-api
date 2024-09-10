package com.otcbridge.controller.v1;

import com.otcbridge.controller.v1.request.CreateExclusionKeywordRequest;
import com.otcbridge.controller.v1.request.DiscardChatsRequest;
import com.otcbridge.controller.v1.request.RetryForUncategorizedChatRequest;
import com.otcbridge.controller.v1.request.SplitMultiBondChatRequest;
import com.otcbridge.controller.v1.response.ExclusionKeywordResponse;
import com.otcbridge.domain.chat.ChatStatus;
import com.otcbridge.service.BondClassifier;
import com.otcbridge.service.ChatService;
import com.otcbridge.service.dto.BondChatDto;
import com.otcbridge.service.dto.ChatDto;
import com.otcbridge.service.dto.ExclusionKeywordDto;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
public class ChatController {

    private final ChatService chatService;
    private final BondClassifier classifier;

    @GetMapping("/api/v1/chat/uncategorized")
    public List<ChatDto> findUncategorizedChats(@RequestParam("date") String date) {
        return chatService.findUncategorizedChats(date);
    }

    @PostMapping("/api/v1/chat/uncategorized/retry")
    public List<BondChatDto> retryAggregation(@RequestBody RetryForUncategorizedChatRequest request) {
        return chatService.retryForUncategorizedChat(request.getDate());
    }

    @GetMapping("/api/v1/chat/multi-bond")
    public List<ChatDto> findMultiBondChats(@RequestParam("date") String date) {
        return chatService.findMultiBondChats(date);
    }

    @PostMapping("/api/v1/chat/multi-bond/split")
    public int splitMultiBondChat(@RequestBody SplitMultiBondChatRequest request) {
        return chatService.split(request.getChatId(), request.getChatDate(), request.getSplitContents());
    }

    @PatchMapping("/api/v1/chat/discard")
    public void discardChats(@RequestParam(name = "status") ChatStatus status, @RequestBody DiscardChatsRequest request) {
        chatService.discardChats(request.getChatIds(), request.getChatDate(), status);
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
