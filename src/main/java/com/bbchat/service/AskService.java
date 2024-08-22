package com.bbchat.service;

import com.bbchat.domain.entity.Ask;
import com.bbchat.repository.AskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
public class AskService {

    private final ChatProcessor chatProcessor;
    private final AskRepository askRepository;

    @Transactional
    public void aggregate(String date) {
        List<Ask> asks = chatProcessor.process(date);
        askRepository.saveAll(asks);
    }

}
