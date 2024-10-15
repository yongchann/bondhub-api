package com.bondhub.controller.v1;

import com.bondhub.controller.v1.request.AppendChatRequest;
import com.bondhub.controller.v1.request.DiscardChatsRequest;
import com.bondhub.controller.v1.request.RetryForUncategorizedChatRequest;
import com.bondhub.controller.v1.request.SplitMultiBondChatRequest;
import com.bondhub.service.ChatService;
import com.bondhub.service.dto.BondChatDto;
import com.bondhub.service.dto.MultiBondChatDto;
import com.bondhub.service.dto.UncategorizedChatDto;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
public class ChatController {

    private final ChatService chatService;

    @PostMapping("/api/v1/chat/recent")
    public void appendRecentChats(@RequestBody AppendChatRequest request) {
        chatService.append(request.getChatDate(), request.getChats());
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

}
