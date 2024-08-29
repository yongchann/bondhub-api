package com.bbchat.controller.v1.request;

import com.bbchat.domain.MaturityCondition;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@AllArgsConstructor
@Getter
public class InquiryAskRequest {

    private MaturityCondition maturityCondition;

    private List<String> grades;

}
