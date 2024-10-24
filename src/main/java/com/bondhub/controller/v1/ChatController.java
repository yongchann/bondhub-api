package com.bondhub.controller.v1;

import com.bondhub.controller.v1.request.AppendChatRequest;
import com.bondhub.controller.v1.request.DiscardChatsRequest;
import com.bondhub.controller.v1.request.RetryForUncategorizedChatRequest;
import com.bondhub.controller.v1.request.SplitMultiBondChatRequest;
import com.bondhub.domain.chat.ChatStatus;
import com.bondhub.service.ChatService;
import com.bondhub.service.dto.ChatGroupByContentDto;
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

    @GetMapping("/api/v1/chat/group")
    public List<ChatGroupByContentDto> groupedChats(@RequestParam("date") String date, @RequestParam("status") ChatStatus status) {
        return chatService.findChatsGroupByContent(date, status);
    }

    @PostMapping("/api/v1/chat/uncategorized/retry")
    public void retryAggregation(@RequestBody RetryForUncategorizedChatRequest request) {
         chatService.retryForUncategorizedChat(request.getDate());
    }

    @PostMapping("/api/v1/chat/multi-bond/split")
    public int splitMultiBondChat(@RequestBody SplitMultiBondChatRequest request) {
        return chatService.split(request.getChatDate(), request.getMultiBondChatId(), request.getSplitContents());
    }

    @PostMapping("/api/v1/chat/multi-bond/auto-split")
    public void autoSplitMultiBondChat(@RequestParam("date") String date, @RequestParam("limit") int limit) {
        chatService.autoSplit(date, limit);
    }

    @PatchMapping("/api/v1/chat/discard")
    public void discardChats(@RequestBody DiscardChatsRequest request) {
        chatService.discardChats(request.getChatDate(), request.getChatIds());
    }

}
