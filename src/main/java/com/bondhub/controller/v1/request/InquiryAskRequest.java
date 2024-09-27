package com.bondhub.controller.v1.request;

import com.bondhub.domain.ask.MaturityCondition;
import com.bondhub.domain.bond.BondType;
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
