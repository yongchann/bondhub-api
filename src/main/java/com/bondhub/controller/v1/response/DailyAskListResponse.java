package com.bondhub.controller.v1.response;

import com.bondhub.domain.ask.Ask;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@AllArgsConstructor
@Getter
public class DailyAskListResponse {

    private List<Ask> publicAsks;
    private List<Ask> bankAsks;
    private List<Ask> specializedCreditAsks;
    private List<Ask> companyAsks;
}
