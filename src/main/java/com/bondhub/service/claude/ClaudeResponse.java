package com.bondhub.service.claude;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;


@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ClaudeResponse {
    private String id;
    private String type;
    private String role;
    private String model;
    private List<ResponseDetail> content;
    private String stop_reason;
    private Object stop_sequence; // null 값 처리
    private Object usage;
}