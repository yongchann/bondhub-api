package com.bondhub.service.analysis;

import com.bondhub.service.claude.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RequiredArgsConstructor
@Component
public class ClaudeClient {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    private static final String API_URL = "https://api.anthropic.com/v1/messages";
    private static final String CLAUDE_MODEL = "claude-3-haiku-20240307";
    private static final String PROMPT = "당신은 채권 브로커의 숙련된 부하직원입니다. 주어진 채권 호가 문자열을 정확하게 분석하고 개별 호가로 분리해야 합니다. 입력: list of {id, content} 예: [{\"id\": \"1\", \"content\": \"25.4.11 우금캐466-1 (민 3.466 금 AA-) 팔자 25.4.25 하나캐394-1 (민 3.443 금 끝전.13 AA-) 팔자 25.4.25 NH농협캐피탈 (민 3.456 금 끝전.21 AA-) 팔자\"}]. 작업: 1. 각 content를 개별 호가로 분리하세요. 각 호가는 \"yy.mm.dd\" 형식의 만기일과 종목명을 포함합니다. 2. 분리된 호가 정보를 JSON 형식으로 구성하세요. 출력: 별도의 설명 없이 다음 형식의 JSON만 반환하세요: [{ \"id\": \"입력된 ID\", \"contents\": [\"분리된 호가1\", \"분리된 호가2\", ...] }, ...]. 주의사항: - 모든 입력에 대해 이 작업을 수행하고 하나의 JSON 배열로 결과를 반환하세요. 출력에 대한 설명을 하지 마세요 - 각 호가는 완전한 형태여야 합니다 (날짜, 종목명, 금리 정보 등 포함). - 띄어쓰기나 특수 문자의 미세한 차이에 주의를 기울이세요. 이 지시사항을 정확히 따라 주어진 입력을 처리해주세요.";
    private static final int value = 2000;

    @Value("${claude.api.key}")
    private String API_KEY;

    public ChatSeparationResponse requestSeparation(ChatSeparationRequest request) {
        try {
            // 요청
            HttpHeaders headers = getHttpHeaders();
            Map<String, Object> requestBody = getStringObjectMap(request.getSimpleMultiBondChats());
            HttpEntity<Map<String, Object>> httpRequest = new HttpEntity<>(requestBody, headers);
            String response = restTemplate.postForObject(API_URL, httpRequest, String.class);

            // 응답
            ClaudeResponse claudeResponse = objectMapper.readValue(response, ClaudeResponse.class);
            List<SeparationResult> multiBondChats = objectMapper.readValue(claudeResponse.getContent().get(0).getText(),
                    objectMapper.getTypeFactory().constructCollectionType(List.class, SeparationResult.class));
            return new ChatSeparationResponse(multiBondChats);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private Map<String, Object> getStringObjectMap(Object message) throws JsonProcessingException {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", CLAUDE_MODEL);
        requestBody.put("max_tokens", value);

        // 시스템 메시지 추가
        List<Map<String, String>> system = new ArrayList<>();
        system.add(Map.of("type", "text", "text", PROMPT));
        requestBody.put("system", system);

        // 사용자 메시지 추가
        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "user", "content", objectMapper.writeValueAsString(message)));
        requestBody.put("messages", messages);

        return requestBody;
    }

    private HttpHeaders getHttpHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-api-key", API_KEY);
        headers.set("anthropic-version", "2023-06-01");
        return headers;
    }

}
