package com.bbchat.service;

import com.bbchat.support.FileInfo;
import com.bbchat.support.FileValidator;
import com.bbchat.support.S3FileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.InputStream;

@Slf4j
@RequiredArgsConstructor
@Service
public class UploadService {

    private final S3FileRepository fileRepository;
    private final FileValidator fileValidator;

    public final static String CHAT_FILE_KEY_PREFIX = "chat";
    public final static String CHAT_FILE_SAVE_NAME = "chat.txt";
    public final static String TRANSACTION_FILE_KEY_PREFIX = "transaction";
    public final static String TRANSACTION_FILE_SAVE_NAME = "transaction.xlsx";

    public void uploadChatFile(String uploadDate, String fileName, InputStream inputStream) {
        FileInfo fileInfo = fileValidator.checkChatFileName(fileName);
        if (!fileInfo.getFileNameDate().equals(uploadDate)) {
            log.warn("this chat file is not created today. dateFromFileName: {}", fileInfo.getFileNameDate());
        }

        String filePath = fileRepository.buildPath(CHAT_FILE_KEY_PREFIX, fileInfo.getFileNameDate(), fileInfo.getFileNamePrefix());
        fileRepository.save(filePath, "chat.txt", inputStream, "text/plain; charset=UTF-8");
    }

    public void uploadTransactionFile(String uploadDate, String fileName, InputStream inputStream) {
        String dateFromFileName =  fileValidator.checkTransactionFileName(fileName);
        if (!dateFromFileName.equals(uploadDate)) {
            log.warn("this excel file is not created today. dateFromFileName: {}", dateFromFileName);
        }

        String filePath = fileRepository.buildPath(TRANSACTION_FILE_KEY_PREFIX, dateFromFileName);
        fileRepository.save(filePath, "transaction.xlsx", inputStream, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
    }

}
