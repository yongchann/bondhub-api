package com.bbchat.controller.v1;

import com.bbchat.controller.v1.response.UploadFileResponse;
import com.bbchat.service.UploadService;
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

    private final UploadService uploadService;

    @PostMapping("/api/v1/upload/chat")
    public ResponseEntity<?> uploadChatFile(@RequestParam("date") String date,
                                            @RequestParam("roomType") String roomType,
                                            @RequestParam("file") MultipartFile file) {
        try {
            uploadService.uploadChatFile(date, roomType, file.getOriginalFilename(), file.getInputStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return ResponseEntity.ok(new UploadFileResponse(file.getOriginalFilename()));
    }

    @PostMapping("/api/v1/upload/transaction")
    public ResponseEntity<?> uploadTransactionFile(@RequestParam("date") String date, @RequestParam("file") MultipartFile file) {
        try {
            uploadService.uploadTransactionFile(date, file.getOriginalFilename(), file.getInputStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return ResponseEntity.ok(new UploadFileResponse(file.getOriginalFilename()));
    }

}
