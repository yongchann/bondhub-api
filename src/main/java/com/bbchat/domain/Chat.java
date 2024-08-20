package com.bbchat.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@AllArgsConstructor
@Getter
@Setter
@ToString
public class Chat {

    private String senderName;

    private String sendDateTime;

    private String dueDate;

    private String content;

    private String senderAddress;

}
