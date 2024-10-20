package com.bondhub.service.claude;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class ChatSeparationResponse {

    private List<SeparationResult> multiBondChats;
}
