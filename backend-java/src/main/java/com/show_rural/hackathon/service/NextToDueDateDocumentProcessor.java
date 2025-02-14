package com.show_rural.hackathon.service;

import com.show_rural.hackathon.domain.Document;
import com.show_rural.hackathon.domain.DocumentStatus;
import com.show_rural.hackathon.repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAmount;
import java.util.List;

import static com.show_rural.hackathon.util.EmailUtil.EXPIRATION_MESSAGE;

@Service
@Slf4j
@RequiredArgsConstructor
public class NextToDueDateDocumentProcessor {
    private final DocumentRepository documentRepository;
    private final EmailService emailService;

    @Scheduled(initialDelay = 0, fixedDelay = 10000)
    @Transactional
    public void checkDocumentsExpiration() {
        setRegularStatus();
        setWarningStatus();
        setUrgentStatus();
    }

    private void setWarningStatus() {
        Integer days = 240;
        List<Document> documents = documentRepository.findNextExpirations(days);

        for (var document : documents) {
            document.setStatus(DocumentStatus.WARNING);
        }
    }

    private void setUrgentStatus() {
        Integer daysToNotify = 120;
        List<Document> documents = documentRepository.findNextExpirations(daysToNotify);

        for (var document : documents) {
            document.setStatus(DocumentStatus.URGENT);

            if (document.getSentMail() != null && document.getSentMail()) {
                continue;
            }

            String subject = "Alerta de Vencimento - Documento " + document.getProtocol();

            String message = EXPIRATION_MESSAGE.formatted(
                    document.getProtocol(),
                    document.getIdentifier().getName(),
                    document.getCnpj(),
                    document.getNumber(),
                    document.getLimitDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                    document.getDocumentUrl()
            );

            try {
                emailService.send(subject, message);
                document.setSentMail(true);
                log.info("Email de alerta enviado com sucesso para documento: {}", document.getProtocol());
            } catch (Exception e) {
                log.error("Erro ao enviar email de alerta para documento: {}", document.getProtocol(), e);
            }
        }
    }

    private void setRegularStatus() {
        List<Document> documents = documentRepository.findAll();
        for (var document : documents) {
            document.setStatus(DocumentStatus.REGULAR);
        }
    }
}
