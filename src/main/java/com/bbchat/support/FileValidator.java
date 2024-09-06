package com.bbchat.support;

import com.bbchat.service.exception.IllegalFileNameException;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

@Component
public class FileValidator {

    private static final Map<String, String> VALID_CHAT_FILENAME_MAP = Map.of(
            "BB", "블커본드",
            "RB", "레드본드",
            "MM", "막무가내"
    );

    public String checkTransactionFileName(String fileName) {
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

    public FileInfo parseChatFileName(String fileName, String roomType) {
        String requiredStr = VALID_CHAT_FILENAME_MAP.get(roomType);
        if (!fileName.contains(requiredStr)) {
            throw new IllegalFileNameException("파일명은 '채권_%s_yyyyMMdd_HHMMSS.txt' 형식이어야 합니다.".formatted(requiredStr));
        }

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
                    .build();
        } catch (ParseException e) {
            throw new IllegalFileNameException("파일명은 '채권_%s_yyyyMMdd_HHMMSS.txt' 형식이어야 합니다.".formatted(requiredStr));
        }
    }

}