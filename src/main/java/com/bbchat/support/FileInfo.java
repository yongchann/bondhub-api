package com.bbchat.support;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.io.InputStream;

@Builder
@AllArgsConstructor
@Getter
public class FileInfo {

    private String filename;
    private String fileNameDate;
    private String fileNamePrefix;
    private long size;
    private String contentType;
    private String content;
    private InputStream inputStream;
}
