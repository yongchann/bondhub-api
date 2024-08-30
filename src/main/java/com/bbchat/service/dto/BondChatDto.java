package com.bbchat.service.dto;

import com.bbchat.domain.bond.Bond;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.*;


@Builder
@AllArgsConstructor
@Getter
public class BondChatDto {

    private String bondName;

    private String dueDate;

    private String grade;

    private List<ChatDto> chats;

    public static BondChatDto from(Bond bond) {
        return BondChatDto.builder()
                .bondName(bond.getBondIssuer().getName())
                .dueDate(bond.getDueDate())
                .grade(bond.getBondIssuer().getGrade())
                .chats(new ArrayList<>())
                .build();
    }

    public void sortChats() {
        List<ChatDto> distinctChats = new HashSet<>(chats).stream().toList();
        chats = distinctChats.stream()
                .sorted(Comparator.comparing(ChatDto::getSendTime)).toList();
    }

}
