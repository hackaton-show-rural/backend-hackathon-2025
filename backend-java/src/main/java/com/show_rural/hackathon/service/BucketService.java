package com.show_rural.hackathon.service;

import org.springframework.web.multipart.MultipartFile;

public interface BucketService {
    String upload(MultipartFile file);
}
