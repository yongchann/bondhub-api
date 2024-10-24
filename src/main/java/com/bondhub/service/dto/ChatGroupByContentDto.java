package com.bondhub.service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
@Getter
public class ChatGroupByContentDto {

    private String content;

    private List<Long> ids;
}
