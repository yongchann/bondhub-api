package com.bbchat.domain;

import com.bbchat.domain.dto.AskDto;
import com.bbchat.domain.dto.MultiDueDateChatDto;
import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
@Getter
public class DailyAskSummary {

    private List<AskDto> publicAsks;

    private List<AskDto> bankAsks;

    private List<AskDto> specializedCreditAsks;

    private List<AskDto> companyAsks;

    private List<MultiDueDateChatDto> multiDueDateChats;
}
