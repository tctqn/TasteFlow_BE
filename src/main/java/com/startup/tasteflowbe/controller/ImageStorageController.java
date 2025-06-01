package com.startup.tasteflowbe.controller;

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
