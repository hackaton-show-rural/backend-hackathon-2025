package com.show_rural.hackathon.service;

import com.show_rural.hackathon.repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailDocumentProcessor {
    private final DocumentRepository documentRepository;

    
}
