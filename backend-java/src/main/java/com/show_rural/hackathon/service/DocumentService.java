package com.show_rural.hackathon.service;

import com.show_rural.hackathon.controller.dto.PageParams;
import com.show_rural.hackathon.domain.Document;
import com.show_rural.hackathon.repository.DocumentRepository;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@Service
@RequiredArgsConstructor
public class DocumentService {
    private final BucketService bucketService;
    private final DocumentExtractor documentExtractor;
    private final DocumentRepository documentRepository;

    public Document upload(MultipartFile file) {
        String documentId = bucketService.upload(file);
        Document document = documentExtractor.processFile(file);

        document.setDocumentId(documentId);
        return documentRepository.save(document);
    }

    public Page<Document> list(PageParams params) {
        var page = PageRequest.of(params.getOffset(), params.getLimit());
        return documentRepository.list(page);
    }
}