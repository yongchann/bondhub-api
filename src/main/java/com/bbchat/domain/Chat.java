package com.bbchat.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class Chat {

    private String senderName;

    private String sendDateTime;

    private String content;

    private String senderAddress;

}
