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

    public void save(String filePath, String fileName, InputStream inputStream, String contentType, String type) {
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(contentType);
        metadata.setContentEncoding("UTF-8");
        metadata.addUserMetadata("type", type);

        String objectKey = filePath + "/" + fileName;
        s3Client.putObject(BUCKET_NAME, objectKey, inputStream, metadata);
    }

    public FileInfo get(String fileDir, String fileName) {
        String filePath = buildPath(fileDir, fileName);
        S3Object object = s3Client.getObject(BUCKET_NAME, filePath);

        long size = object.getObjectMetadata().getContentLength();
        String contentType = object.getObjectMetadata().getContentType();
        String content = "";
        InputStream inputStream = null;

        String type = object.getObjectMetadata().getUserMetadata().get("type");
        if (type == null) {
            throw new IllegalStateException("empty s3 file metadata");
        }

        if (type.equals("chat")) {
            try {
                content = IOUtils.toString(object.getObjectContent());
            } catch (IOException e) {
                throw new IllegalStateException("failed to read file content");
            }
        } else if (type.equals("transaction")) {
            inputStream = object.getObjectContent();
        } else {
            throw new IllegalStateException("invalid s3 file metadata");
        }

        return FileInfo.builder()
                .filename(fileName)
                .size(size)
                .contentType(contentType)
                .inputStream(inputStream)
                .content(content)
                .build();
    }

    public static String buildPath(String... paths) {
        return String.join("/", paths);
    }

}
