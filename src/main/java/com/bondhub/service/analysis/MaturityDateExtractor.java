package com.bondhub.service.analysis;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class MaturityDateExtractor {

    private static final String MATURITY_PATTERN = "(\\d{2})([./ ])(0?[1-9]|1[0-2])\\2(0?[1-9]|[12][0-9]|3[01])(?!\\d)";

    private static final Pattern PATTERN = Pattern.compile(MATURITY_PATTERN);

    public List<String> extractAllMaturities(String content) {
        Matcher matcher = PATTERN.matcher(content);
        List<String> dates = new ArrayList<>();
        while (matcher.find()) {
            dates.add(standardizeMaturityDate(matcher.group()));
        }
        return dates;
    }

    private String standardizeMaturityDate(String date) {
        date = date.replace('.', '-').replace('/', '-').replace(' ', '-');

        String[] parts = date.split("-");
        if (parts[0].length() == 2) parts[0] = "20" + parts[0]; // 2자리 연도 처리
        if (parts[1].length() == 1) parts[1] = "0" + parts[1];  // 1자리 월 처리
        if (parts[2].length() == 1) parts[2] = "0" + parts[2];  // 1자리 일 처리

        return String.join("-", parts);  // YYYY-MM-DD 형식으로 반환
    }
}
