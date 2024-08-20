package com.bbchat.support;

import com.bbchat.domain.Chat;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;

@Component
public class ChatStreamConverter {

    public InputStream convertListToInputStream(List<Chat> chatList) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonString = objectMapper.writeValueAsString(chatList);

        return new ByteArrayInputStream(jsonString.getBytes());
    }
}
