package com.bbchat.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class AggregationService {

    private final ChatProcessor chatProcessor;

    public void aggregate(String date) {
        chatProcessor.process(date);
    }
}
