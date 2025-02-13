package com.show_rural.hackathon.controller;

import com.show_rural.hackathon.controller.dto.DocumentFilters;
import com.show_rural.hackathon.controller.dto.PageParams;
import com.show_rural.hackathon.domain.Document;
import com.show_rural.hackathon.service.DocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


@RestController
@RequestMapping("documents")
@RequiredArgsConstructor
public class DocumentController {
    private final DocumentService documentService;

    @GetMapping
    public ResponseEntity<Page<Document>> list(@ModelAttribute PageParams params,
                                               @ModelAttribute DocumentFilters filters) {
        return ResponseEntity.ok(documentService.list(params, filters));
    }

    @PostMapping("/upload")
    public ResponseEntity<List<Document>> upload(@RequestParam("files") List<MultipartFile> files) {
        return ResponseEntity.ok(documentService.upload(files));
    }
}
