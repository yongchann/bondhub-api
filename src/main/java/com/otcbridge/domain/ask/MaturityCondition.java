package com.otcbridge.domain.ask;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@AllArgsConstructor
@Getter
public class MaturityCondition {

    private String maturityInquiryType;

    private String minYear;

    private String maxYear;

    private String minMonth;

    private String maxMonth;

}
