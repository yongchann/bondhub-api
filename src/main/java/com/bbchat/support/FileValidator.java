package com.bbchat.support;

import com.bbchat.service.exception.IllegalFileNameException;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Component
public class FileValidator {

    private static final List<String> ALLOWED_CHAT_FILE_PREFIX = List.of("BB", "RB", "MM");
    private static final List<String> ALLOWED_TRANSACTION_FILE_PREFIX = Arrays.asList("TX");

    public FileInfo checkChatFileName(String fileName) {
        for (String prefix : ALLOWED_CHAT_FILE_PREFIX) {
            if (fileName.startsWith(prefix)) {
                return parseChatFileName(fileName, prefix);
            }
        }
        throw new IllegalFileNameException(fileName);
    }

    public String checkTransactionFileName(String fileName) {
        if (ALLOWED_TRANSACTION_FILE_PREFIX.stream().noneMatch(fileName::startsWith)) {
            throw new IllegalFileNameException(fileName);
        }

        String[] parts = fileName.split("_");
        if (parts.length < 2) {
            throw new IllegalFileNameException(fileName);
        }
        String dateString = parts[1].split("\\.")[0];
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyyMMdd");
            SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd");
            Date date = inputFormat.parse(dateString);
            return outputFormat.format(date);
        } catch (ParseException e) {
            throw new IllegalFileNameException(fileName);
        }
    }

    public FileInfo parseChatFileName(String fileName, String prefix) {
        String[] split = fileName.split("_");

        String dateString = split[2];

        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
            Date date = dateFormat.parse(dateString);
            SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd");
            String formattedDate = outputFormat.format(date);

            return FileInfo.builder()
                    .filename(fileName)
                    .fileNameDate(formattedDate)
                    .fileNamePrefix(prefix)
                    .build();
        } catch (ParseException e) {
            throw new IllegalFileNameException(fileName);
        }
    }

}