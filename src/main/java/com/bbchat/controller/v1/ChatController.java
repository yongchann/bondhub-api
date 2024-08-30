package com.bbchat.controller.v1;

import com.bbchat.service.ChatService;
import com.bbchat.service.dto.ChatDto;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RestController
public class ChatController {

    private final ChatService chatService;

    @GetMapping("/api/v1/chat/uncategorized")
    public List<String> findUncategorizedChats(@RequestParam("date") String date, @RequestParam("roomType") String roomType) {
        return chatService.findUncategorizedChats(date, roomType);
    }

    @GetMapping("/api/v1/chat/multi-bond")
    public List<ChatDto> findMultiBondChats(@RequestParam("date") String date, @RequestParam("roomType") String roomType) {
        return chatService.findMultiBondChats(date, roomType);
    }

}
