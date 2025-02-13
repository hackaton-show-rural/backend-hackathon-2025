package com.show_rural.hackathon.service;

import com.show_rural.hackathon.controller.dto.DocumentFilters;
import com.show_rural.hackathon.controller.dto.PageParams;
import com.show_rural.hackathon.domain.Document;
import com.show_rural.hackathon.repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.*;

@Service
@RequiredArgsConstructor
public class DocumentService {
    private final BucketService bucketService;
    private final DocumentExtractor documentExtractor;
    private final DocumentRepository documentRepository;

    public List<Document> upload(List<MultipartFile> files) {
        List<Document> documents = new ArrayList<>();

        for (var file : files) {
            Document document = upload(file);
            documents.add(document);
        }

        return documents;
    }

    public Document upload(MultipartFile file) {
        String url = bucketService.upload(file);
        Document document = documentExtractor.processFile(file);

        document.setDocumentUrl(url);
        return documentRepository.save(document);
    }

    public void upload(File file) {
        String url = bucketService.upload(file);
        Document document = documentExtractor.processFile(file);

        document.setDocumentUrl(url);
        documentRepository.save(document);
    }

    public Page<Document> list(PageParams params, DocumentFilters filters) {
        var page = PageRequest.of(params.getOffset(), params.getLimit());
        return documentRepository.list(page, filters);
    }
}