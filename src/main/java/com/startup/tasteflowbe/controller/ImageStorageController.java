package com.startup.tasteflowbe.controller;

import com.startup.tasteflowbe.model.ImageStorage;
import com.startup.tasteflowbe.service.ImageStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/images")
@RequiredArgsConstructor
public class ImageStorageController {

    private final ImageStorageService imageService;

    @PostMapping("/upload")
    public ResponseEntity<ImageStorage> uploadImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam("relatedTable") String relatedTable,
            @RequestParam("recordId") Long recordId
    ) {
        try {
            ImageStorage saved = imageService.uploadImage(file, relatedTable, recordId);
            return ResponseEntity.ok(saved);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
