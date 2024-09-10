package com.otcbridge.controller.v1.request;

import com.otcbridge.domain.MaturityCondition;
import com.otcbridge.domain.bond.BondType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@AllArgsConstructor
@Getter
public class InquiryAskRequest {

    private MaturityCondition maturityCondition;

    private BondType bondType;

    private List<String> grades;

}
