package com.bbchat.support;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.util.IOUtils;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;

@RequiredArgsConstructor
@Component
public class S3FileRepository {

    private final AmazonS3Client s3Client;
    private final static String BUCKET_NAME = "bbchat-bucket";
    private final static String CHAT_FILE_KEY_PREFIX = "chat";
    private final static String TRANSACTION_FILE_KEY_PREFIX = "transaction";

    public void saveChatFile(String date, String fileName, InputStream inputStream, String contentType) {
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(contentType);
        metadata.setContentEncoding("UTF-8");
        s3Client.putObject(BUCKET_NAME, buildPath(date, CHAT_FILE_KEY_PREFIX, fileName), inputStream, metadata);
    }

    public void saveExcelFile(String date, String fileName, InputStream inputStream, String contentType) {
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(contentType);
        metadata.setContentEncoding("UTF-8");
        s3Client.putObject(BUCKET_NAME, buildPath(date, TRANSACTION_FILE_KEY_PREFIX, fileName), inputStream, metadata);
    }

    public FileInfo getChatFileByDate(String date) {
        S3Object object = s3Client.getObject(BUCKET_NAME, buildPath(date, CHAT_FILE_KEY_PREFIX, "ORIGINAL.txt"));

        long size = object.getObjectMetadata().getContentLength();
        String contentType = object.getObjectMetadata().getContentType();
        String content = null;
        try {
            content = IOUtils.toString(object.getObjectContent());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return FileInfo.builder()
                .filename(date)
                .size(size)
                .contentType(contentType)
                .content(content)
                .build();
    }

    public FileInfo getTransactionFileByDate(String date) {
        S3Object object = s3Client.getObject(BUCKET_NAME, buildPath(date, TRANSACTION_FILE_KEY_PREFIX, "ORIGINAL.xlsx"));
        long size = object.getObjectMetadata().getContentLength();
        String contentType = object.getObjectMetadata().getContentType();
        InputStream inputStream = object.getObjectContent();

        return FileInfo.builder()
                .filename(date)
                .size(size)
                .contentType(contentType)
                .content("")
                .inputStream(inputStream)
                .build();
    }

    private String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return "";
        }
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    return String.valueOf(cell.getNumericCellValue());
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return "";
        }
    }

    public String buildPath(String... paths) {
        return String.join("/", paths);
    }
}
