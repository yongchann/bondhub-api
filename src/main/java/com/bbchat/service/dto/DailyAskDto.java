package com.bbchat.service.dto;

import com.bbchat.domain.ask.DailyAsk;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.*;

@AllArgsConstructor
@Builder
@Getter
public class DailyAskDto {

    private String bondName;

    private String dueDate;

    private String grade;

    private int consecutiveDays;

    private List<ChatDto> chats;

    public static DailyAskDto from(DailyAsk dailyAsk) {
        return DailyAskDto.builder()
                .bondName(dailyAsk.getBond().getBondIssuer().getName())
                .grade(dailyAsk.getBond().getBondIssuer().getGrade())
                .dueDate(dailyAsk.getBond().getDueDate())
                .consecutiveDays(dailyAsk.getConsecutiveDays())
                .chats(new ArrayList<>())
                .build();
    }

    public void sortChats() {
        List<ChatDto> distinctChats = new HashSet<>(chats).stream().toList(); // 중복 제거
        chats = distinctChats.stream().toList().stream()
                .sorted(Comparator.comparing(ChatDto::getSendTime).reversed()).toList(); // sendTime 역순 정렬
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DailyAskDto that = (DailyAskDto) o;
        return Objects.equals(bondName, that.bondName) && Objects.equals(dueDate, that.dueDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(bondName, dueDate);
    }

}
