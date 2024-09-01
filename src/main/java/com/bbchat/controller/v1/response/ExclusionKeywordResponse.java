package com.bbchat.controller.v1.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
@Getter
public class ExclusionKeywordResponse {

    private List<String> keywords;
}
