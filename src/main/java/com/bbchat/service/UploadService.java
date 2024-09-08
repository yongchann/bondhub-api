package com.bbchat.service;

import com.bbchat.service.exception.IllegalFileNameException;
import com.bbchat.support.FileInfo;
import com.bbchat.support.FileValidator;
import com.bbchat.support.S3FileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
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

    public void uploadChatFile(String uploadDate, String roomType, String fileName, InputStream inputStream) throws IOException {
        FileInfo fileInfo = fileValidator.parseChatFileName(fileName, roomType);
        if (!fileInfo.getFileNameDate().equals(uploadDate)) {
            log.warn("this chat file is not created today. dateFromFileName: {}", fileInfo.getFileNameDate());
            throw new IllegalFileNameException("선택된 날짜와 파일의 날짜가 일치하지 않습니다.");
        }

        String filePath = S3FileRepository.buildPath(CHAT_FILE_KEY_PREFIX, fileInfo.getFileNameDate(), roomType);
        byte[] bytes = inputStream.readAllBytes();
        String content = new String(bytes, "EUC-KR");
        InputStream utf8InputStream = new ByteArrayInputStream(content.getBytes());

        fileRepository.saveChatFile(filePath, fileName, utf8InputStream, "text/plain; charset=UTF-8", "chat");
    }

    public void uploadTransactionFile(String uploadDate, String fileName, InputStream inputStream) {
        String dateFromFileName =  fileValidator.parseTransactionFileName(fileName);
        if (!dateFromFileName.equals(uploadDate)) {
            log.warn("this excel file is not created today. dateFromFileName: {}", dateFromFileName);
            throw new IllegalFileNameException("선택된 날짜와 파일의 날짜가 일치하지 않습니다.");
        }

        String filePath = S3FileRepository.buildPath(TRANSACTION_FILE_KEY_PREFIX, dateFromFileName);
        fileRepository.saveTransactionFile(filePath, "transaction.xlsx", inputStream, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "transaction");
    }

}
