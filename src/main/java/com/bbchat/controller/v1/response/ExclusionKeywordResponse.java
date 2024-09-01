package com.bbchat.controller.v1.response;

import com.bbchat.service.dto.ExclusionKeywordDto;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
@Getter
public class ExclusionKeywordResponse {

    private List<ExclusionKeywordDto> keywords;
}
