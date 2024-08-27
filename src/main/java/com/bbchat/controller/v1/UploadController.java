package com.bbchat.controller.v1;

import com.bbchat.controller.v1.response.UploadFileResponse;
import com.bbchat.support.S3FileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RequiredArgsConstructor
@RestController
public class UploadController {

    private final S3FileRepository fileRepository;

    @PostMapping("/api/v1/upload/chat")
    public ResponseEntity<?> uploadChatFile(@RequestParam("date") String date, @RequestParam("file") MultipartFile file) {
        try {
            fileRepository.saveChatFile(date, "ORIGINAL.txt", file.getInputStream(), "text/plain; charset=UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        return ResponseEntity.ok(new UploadFileResponse(date));
    }

    @PostMapping("/api/v1/upload/transaction")
    public ResponseEntity<?> uploadTransactionFile(@RequestParam("date") String date, @RequestParam("file") MultipartFile file) {
        try {
            fileRepository.saveExcelFile(date, "ORIGINAL.xlsx", file.getInputStream(), "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        return ResponseEntity.ok(new UploadFileResponse(date));
    }

}
