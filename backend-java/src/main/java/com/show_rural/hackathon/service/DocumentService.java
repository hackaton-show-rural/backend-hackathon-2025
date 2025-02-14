package com.show_rural.hackathon.service;

import com.show_rural.hackathon.controller.dto.*;
import com.show_rural.hackathon.domain.Document;
import com.show_rural.hackathon.domain.DocumentStatus;
import com.show_rural.hackathon.repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.time.LocalDate;
import java.time.Month;
import java.util.*;
import java.util.stream.Collectors;

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

        return save(document, url);
    }

    public void upload(File file) {
        String url = bucketService.upload(file);
        Document document = documentExtractor.processFile(file);

        save(document, url);
    }

    private Document save(Document document, String url) {
        document.setDocumentUrl(url);

        if (limitDateIsAfter(document, 120)) {
            document.setStatus(DocumentStatus.URGENT);
        } else if (limitDateIsAfter(document, 240)) {
            document.setStatus(DocumentStatus.WARNING);
        } else {
            document.setStatus(DocumentStatus.REGULAR);
        }

        return documentRepository.save(document);
    }

    private boolean limitDateIsAfter(Document document, int days) {
        return document.getLimitDate().isBefore(LocalDate.now().plusDays(days)) ||
                document.getLimitDate().isEqual(LocalDate.now().plusDays(days));
    }

    public Page<Document> list(PageParams params, DocumentFilters filters) {
        var page = PageRequest.of(params.getOffset(), params.getLimit());
        return documentRepository.list(page, filters);
    }

    public List<DocumentsCityQuantity> countDocPerCity() {
        List<Document> documents = documentRepository.findAll();

        return documents.stream()
                .collect(Collectors.groupingBy(
                        doc -> doc.getIdentifier().getCity(),
                        Collectors.counting()
                ))
                .entrySet()
                .stream()
                .map(entry -> {
                    DocumentsCityQuantity cityCount = new DocumentsCityQuantity();
                    cityCount.setCity(entry.getKey());
                    cityCount.setQuantity(entry.getValue());
                    return cityCount;
                })
                .collect(Collectors.toList());
    }

    public List<DocumentsStatusQuantity> countDocPerStatus() {
        List<Document> documents = documentRepository.findAll();

        return documents.stream()
                .collect(Collectors.groupingBy(
                        Document::getStatus,
                        Collectors.counting()
                ))
                .entrySet()
                .stream()
                .map(entry -> {
                    var statusCount = new DocumentsStatusQuantity();
                    statusCount.setStatus(entry.getKey());
                    statusCount.setQuantity(entry.getValue());
                    return statusCount;
                })
                .collect(Collectors.toList());
    }

    public List<DocumentMonthLimit> countDocPerMonth() {
        List<Document> documents = documentRepository.findAll();

        return documents.stream()
                .collect(Collectors.groupingBy(
                        doc -> doc.getLimitDate().getMonth(),
                        Collectors.counting()
                ))
                .entrySet()
                .stream()
                .map(entry -> {
                    var monthLimit = new DocumentMonthLimit();
                    monthLimit.setMonth(entry.getKey().toString());
                    monthLimit.setQuantity(entry.getValue());
                    return monthLimit;
                })
                .sorted(Comparator.comparing(doc -> Month.valueOf(doc.getMonth())))
                .collect(Collectors.toList());
    }
}