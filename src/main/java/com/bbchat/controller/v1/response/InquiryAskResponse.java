package com.bbchat.controller.v1.response;

import com.bbchat.service.dto.DailyAskDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Builder
@AllArgsConstructor
@Getter
public class InquiryAskResponse{

    private List<DailyAskDto> asks;

    private String fileName;

    private LocalDateTime lastAggregatedDateTime;

    private long totalChatCount;

    private long fullyProcessedChatCount;


}
