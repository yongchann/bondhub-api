package com.bbchat.service;

import com.bbchat.domain.entity.Ask;
import com.bbchat.domain.repository.AskRepository;
import com.bbchat.support.FileInfo;
import com.bbchat.support.S3FileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
public class AskService {

    private final ChatProcessor chatProcessor;
    private final S3FileRepository fileRepository;
    private final AskRepository askRepository;

    @Transactional
    public void aggregate(String date) {
        FileInfo fileInfo = fileRepository.getChatFileByDate(date);
        List<Ask> asks = chatProcessor.process(date, fileInfo.getContent());
        askRepository.saveAll(asks);
    }

}
