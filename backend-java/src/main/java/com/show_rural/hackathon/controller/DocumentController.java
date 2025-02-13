package com.show_rural.hackathon.controller;

import com.show_rural.hackathon.service.DocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;

    @PostMapping("/upload")
    public ResponseEntity<String> uploadDocument(@RequestParam("files") List<MultipartFile> files) {
        List<String> documentsIds = documentService.upload(files);

        StringBuilder result = new StringBuilder();
        for (var documentId : documentsIds) {
            result.append("Document uploaded: ").append(documentId).append("\n");
        }

        return ResponseEntity.ok(result.toString());
    }
}
