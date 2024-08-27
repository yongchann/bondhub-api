package com.bbchat.service.event;

import org.springframework.context.ApplicationEvent;

public class BondAliasAddedEvent extends ApplicationEvent {

    private String message;

    public BondAliasAddedEvent(Object source, String message) {
        super(source);
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
