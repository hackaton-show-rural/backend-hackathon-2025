package com.show_rural.hackathon.service;

import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.errors.*;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.framework.qual.RequiresQualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@RequiredArgsConstructor
public class MinIOIBucketImpl implements BucketService {
    @Value("${minio.bucket-name}")
    private String bucketName;

    private final MinioClient minioClient;

    public String upload(MultipartFile file) {
        log.info("Iniciando upload de MultipartFile. Nome original: {}, Tamanho: {} bytes",
                file.getOriginalFilename(), file.getSize());

        try {
            String documentId = UUID.randomUUID().toString();
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String objectName = documentId + extension;

            log.debug("Preparando upload para MinIO - Document ID: {}, Nome do objeto: {}",
                    documentId, objectName);

            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .stream(file.getInputStream(), file.getSize(), -1)
                    .contentType(file.getContentType())
                    .build());

            log.info("Upload concluído com sucesso. Bucket: {}, Objeto: {}", bucketName, objectName);

            return getUrl(objectName);
        } catch (IOException e) {
            log.error("Erro ao ler o arquivo para upload: {}", file.getOriginalFilename(), e);
            throw new RuntimeException("Falha ao ler arquivo para upload", e);
        } catch (Exception e) {
            log.error("Erro ao fazer upload do arquivo: {} para o MinIO. Detalhes do erro: {}",
                    file.getOriginalFilename(), e.getMessage(), e);
            throw new RuntimeException("Falha no upload do documento", e);
        }
    }

    public String upload(File file) {
        log.info("Iniciando upload de File. Nome: {}, Tamanho: {} bytes",
                file.getName(), file.length());

        String documentId = UUID.randomUUID().toString();
        String originalFilename = file.getName();
        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String objectName = documentId + extension;

        log.debug("Preparando upload para MinIO - Document ID: {}, Nome do objeto: {}",
                documentId, objectName);

        try (FileInputStream inputStream = new FileInputStream(file)) {
            log.debug("Stream do arquivo aberto com sucesso. Iniciando upload para bucket: {}",
                    bucketName);

            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .stream(inputStream, file.length(), -1)
                            .contentType("application/pdf")
                            .build());

            log.info("Upload do arquivo concluído com sucesso. Bucket: {}, Objeto: {}",
                    bucketName, objectName);

            return getUrl(objectName);

        } catch (FileNotFoundException e) {
            log.error("Arquivo não encontrado: {}", file.getAbsolutePath(), e);
            throw new RuntimeException("Arquivo não encontrado para upload", e);
        } catch (IOException e) {
            log.error("Erro de I/O ao ler o arquivo: {}", file.getName(), e);
            throw new RuntimeException("Erro ao ler arquivo para upload", e);
        } catch (Exception e) {
            log.error("Erro ao fazer upload do arquivo: {} para o MinIO. Detalhes do erro: {}",
                    file.getName(), e.getMessage(), e);
            throw new RuntimeException("Não foi possível fazer upload do arquivo", e);
        }
    }

    private String getUrl(String objectName) {
        return String.format("%s/%s/%s", "http://localhost:9000", bucketName, objectName);
    }
}