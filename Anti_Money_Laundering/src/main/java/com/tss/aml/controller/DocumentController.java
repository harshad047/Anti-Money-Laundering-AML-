package com.tss.aml.controller;

import com.tss.aml.entity.Document;
import com.tss.aml.service.DocumentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/documents")
@CrossOrigin(origins = "http://127.0.0.1:5500") // Adjust to your frontend URL
public class DocumentController {

    @Autowired
    private DocumentService documentService;

    @PostMapping(path = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadDocument(@RequestParam("file") MultipartFile file,
                                            @RequestParam("docType") String docType,
                                            Authentication authentication) {
        // Get the authenticated user's email from the security context
        String userEmail = authentication.getName();

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "File is empty."));
        }

        try {
            Document savedDocument = documentService.uploadAndSaveDocument(userEmail, file, docType);
            return ResponseEntity.ok(Map.of(
                    "message", docType + " uploaded successfully!",
                    "documentId", savedDocument.getId(),
                    "url", savedDocument.getStoragePath()
            ));
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to upload file: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}