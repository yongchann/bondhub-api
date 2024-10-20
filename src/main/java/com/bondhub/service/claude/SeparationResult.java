package com.bondhub.service.claude;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@Getter
@NoArgsConstructor
public class SeparationResult {

    private Long id;
    private List<String> contents;
}
