package com.otcbridge.service.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class BondAliasEvent extends ApplicationEvent {

    private Type type;

    private String message;

    private String bondAliasName;

    public enum Type {
        INITIALIZED, CREATED, DELETED,
    }

    public BondAliasEvent(Object source, Type type, String message, String bondAliasName) {
        super(source);
        this.type = type;
        this.message = message;
        this.bondAliasName = bondAliasName;
    }
}
