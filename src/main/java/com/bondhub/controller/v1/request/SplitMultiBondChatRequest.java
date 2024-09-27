package com.bondhub.controller.v1.request;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
@Getter
public class SplitMultiBondChatRequest {

    private List<Long> ids;

    private String originalContent;

    private String chatDate;

    private List<String> splitContents;

}
