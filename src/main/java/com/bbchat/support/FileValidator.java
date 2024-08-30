package com.bbchat.support;

import com.bbchat.service.exception.IllegalFileNameException;
import org.springframework.stereotype.Component;

import java.text.Normalizer;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Component
public class FileValidator {

    private static final List<String> ALLOWED_CHAT_FILE_PREFIX = Arrays.asList("채권_블커본드", "채권_레드본드", "채권_막무가내");
    private static final List<String> ALLOWED_TRANSACTION_FILE_PREFIX = Arrays.asList("거래내역");

    public FileInfo checkChatFileName(String fileName) {
        for (String prefix : ALLOWED_CHAT_FILE_PREFIX) {
            if (fileName.startsWith(prefix)) {
                return parseChatFileName(fileName);
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
        String dateString = parts[1];
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyyMMdd");
            SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd");
            Date date = inputFormat.parse(dateString);
            return outputFormat.format(date);
        } catch (ParseException e) {
            throw new IllegalFileNameException(fileName);
        }
    }

    public FileInfo parseChatFileName(String fileName) {
        String[] split = fileName.split("_");

        String roomType = split[1];
        String dateString = split[2];

        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
            Date date = dateFormat.parse(dateString);
            SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd");
            String formattedDate = outputFormat.format(date);

            return FileInfo.builder()
                    .filename(fileName)
                    .fileNameDate(formattedDate)
                    .fileNamePrefix(convertPrefix(roomType))
                    .build();
        } catch (ParseException e) {
            throw new IllegalFileNameException(fileName);
        }
    }

    private String convertPrefix(String prefix) {
        String normalizedPrefix = Normalizer.normalize(prefix, Normalizer.Form.NFC);

        return switch (normalizedPrefix) {
            case "블커본드" -> "BB";
            case "례드본드" -> "RB";
            case "막무가내" -> "MM";
            default -> throw new IllegalFileNameException(prefix);
        };
    }

}