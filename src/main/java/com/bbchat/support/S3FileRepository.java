package com.bbchat.support;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.util.IOUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;

@RequiredArgsConstructor
@Component
public class S3FileRepository {

    private final AmazonS3Client s3Client;
    private final static String BUCKET_NAME = "bbchat-bucket";

    public void save(String filePath, String fileName, InputStream inputStream, String contentType) {
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(contentType);
        metadata.setContentEncoding("UTF-8");

        String objectKey = filePath + "/" + fileName;
        s3Client.putObject(BUCKET_NAME, objectKey, inputStream, metadata);
    }

    public FileInfo get(String fileDir, String fileName) {
        String filePath = buildPath(fileDir, fileName);
        S3Object object = s3Client.getObject(BUCKET_NAME, filePath);

        long size = object.getObjectMetadata().getContentLength();
        String contentType = object.getObjectMetadata().getContentType();
        String content = null;
        InputStream inputStream = object.getObjectContent();

        try {
            content = IOUtils.toString(object.getObjectContent());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return FileInfo.builder()
                .filename(fileName)
                .size(size)
                .contentType(contentType)
                .inputStream(inputStream)
                .content(content)
                .build();
    }
//
//    public FileInfo getChatFileByDate(String date) {
//        S3Object object = s3Client.getObject(BUCKET_NAME, buildPath(date, CHAT_FILE_KEY_PREFIX, "ORIGINAL.txt"));
//
//        long size = object.getObjectMetadata().getContentLength();
//        String contentType = object.getObjectMetadata().getContentType();
//        String content = null;
//        try {
//            content = IOUtils.toString(object.getObjectContent());
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//
//        return FileInfo.builder()
//                .filename(date)
//                .size(size)
//                .contentType(contentType)
//                .content(content)
//                .build();
//    }
//
//    public FileInfo getTransactionFileByDate(String date,) {
//        S3Object object = s3Client.getObject(BUCKET_NAME, buildPath(date, TRANSACTION_FILE_KEY_PREFIX, "ORIGINAL.xlsx"));
//        long size = object.getObjectMetadata().getContentLength();
//        String contentType = object.getObjectMetadata().getContentType();
//        InputStream inputStream = object.getObjectContent();
//
//        return FileInfo.builder()
//                .filename(date)
//                .size(size)
//                .contentType(contentType)
//                .content("")
//                .inputStream(inputStream)
//                .build();
//    }
    public static String buildPath(String... paths) {
        return String.join("/", paths);
    }

}
