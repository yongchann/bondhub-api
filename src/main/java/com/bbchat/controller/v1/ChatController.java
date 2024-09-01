package com.bbchat.controller.v1;

import com.bbchat.controller.v1.request.DiscardChatsRequest;
import com.bbchat.controller.v1.request.SplitMultiBondChatRequest;
import com.bbchat.controller.v1.response.ExclusionKeywordResponse;
import com.bbchat.domain.chat.ChatStatus;
import com.bbchat.service.BondClassifier;
import com.bbchat.service.ChatService;
import com.bbchat.service.dto.ChatDto;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
public class ChatController {

    private final ChatService chatService;
    private final BondClassifier classifier;

    @GetMapping("/api/v1/chat/uncategorized")
    public List<ChatDto> findUncategorizedChats(@RequestParam("date") String date, @RequestParam("roomType") String roomType) {
        return chatService.findUncategorizedChats(date, roomType);
    }

    @GetMapping("/api/v1/chat/multi-bond")
    public List<ChatDto> findMultiBondChats(@RequestParam("date") String date, @RequestParam("roomType") String roomType) {
        return chatService.findMultiBondChats(date, roomType);
    }

    @PostMapping("/api/v1/chat/multi-bond/split")
    public int splitMultiBondChat(@RequestBody SplitMultiBondChatRequest request) {
        return chatService.split(request.getChatId(), request.getChatDate(), request.getRoomType(), request.getSplitContents());
    }

    @PatchMapping("/api/v1/chat/discard")
    public void discardChats(@RequestParam(name = "status") ChatStatus status, @RequestBody DiscardChatsRequest request) {
        chatService.discardChats(request.getChatIds(), request.getChatDate(), request.getRoomType(), status);
    }

    @GetMapping("/api/v1/chat/exclusion-keyword")
    public ExclusionKeywordResponse getExclusionKeywords() {
        List<String> exclusionKeywords = classifier.getExclusionKeywords();
        return new ExclusionKeywordResponse(exclusionKeywords);
    }
}
