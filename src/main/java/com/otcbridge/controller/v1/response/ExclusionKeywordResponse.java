package com.otcbridge.controller.v1.response;

import com.otcbridge.service.dto.ExclusionKeywordDto;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
@Getter
public class ExclusionKeywordResponse {

    private List<ExclusionKeywordDto> keywords;
}