package com.bondhub.service.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class ExclusionKeywordEvent extends ApplicationEvent {

    private Type type;

    private String message;

    private String keyword;

    public enum Type {
        INITIALIZED, CREATED, DELETED,
    }

    public ExclusionKeywordEvent(Object source, Type type, String message, String keyword) {
        super(source);
        this.type = type;
        this.message = message;
        this.keyword = keyword;
    }

}
