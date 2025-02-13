package com.show_rural.hackathon.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.File;

public interface BucketService {
    String upload(MultipartFile file);

    String upload(File file);
}
