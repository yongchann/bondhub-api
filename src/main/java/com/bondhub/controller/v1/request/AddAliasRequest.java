package com.bondhub.controller.v1.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class AddAliasRequest {

    private String name;

    @JsonCreator
    public AddAliasRequest(@JsonProperty("name") String name) {
        this.name = name;
    }
}
