package com.bbchat.support;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.util.IOUtils;
import jdk.jfr.ContentType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;

@RequiredArgsConstructor
@Component
public class S3FileRepository {

    private final AmazonS3Client s3Client;
    private final static String BUCKET_NAME = "bbchat-bucket";
    private final static String CHAT_FILE_KEY_PREFIX = "chat/";

    public void saveChatFile(String date, InputStream inputStream, String contentType) {
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(contentType);
        metadata.setContentEncoding("UTF-8");
        s3Client.putObject(BUCKET_NAME, CHAT_FILE_KEY_PREFIX + date, inputStream, metadata);
    }

    public FileInfo getChatFileByDate(String date) {
        S3Object object = s3Client.getObject(BUCKET_NAME, CHAT_FILE_KEY_PREFIX+date+"/ORIGINAL.txt");

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

}
