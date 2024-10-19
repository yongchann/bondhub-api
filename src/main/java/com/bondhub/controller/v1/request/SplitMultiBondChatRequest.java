package com.bondhub.controller.v1.request;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
@Getter
public class SplitMultiBondChatRequest {

    private Long multiBondChatId;

    private String originalContent;

    private String chatDate;

    private List<String> splitContents;

}
