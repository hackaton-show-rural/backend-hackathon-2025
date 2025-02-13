package com.show_rural.hackathon.service;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.search.FromTerm;
import jakarta.mail.search.SearchTerm;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.Properties;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmailDocumentProcessor {
    private static final String EMAIL_HOST = "imap.gmail.com";
    private static final String EMAIL_USERNAME = "contato@folhastech.com";
    private static final String EMAIL_PASSWORD = System.getenv("EMAIL_PASSWORD");
    private static final String SENDER_EMAIL = "rafaelbortoli21@gmail.com";

    private final DocumentService documentService;

    @Scheduled(initialDelay = 0, fixedDelay = 5000)
    public void checkInbox() {
        try {
            Properties properties = new Properties();
            properties.put("mail.store.protocol", "imaps");
            properties.put("mail.imaps.host", EMAIL_HOST);
            properties.put("mail.imaps.port", "993");
            properties.put("mail.imaps.ssl.enable", "true");

            Session session = Session.getInstance(properties);
            Store store = session.getStore("imaps");

            store.connect(EMAIL_HOST, EMAIL_USERNAME, EMAIL_PASSWORD);

            Folder inbox = store.getFolder("INBOX");
            inbox.open(Folder.READ_WRITE);

            SearchTerm searchTerm = new FromTerm(new InternetAddress(SENDER_EMAIL));
            Message[] messages = inbox.search(searchTerm);

            for (Message message : messages) {
                if (!message.isSet(Flags.Flag.SEEN)) {
                    log.info("Iniciando verificação de e-mails em: {}", EMAIL_HOST);
                    log.info("Processando novo e-mail - Assunto: '{}' - Recebido em: {}",
                            message.getSubject(),
                            message.getReceivedDate());
                    processMessage(message);
                    message.setFlag(Flags.Flag.SEEN, true);
                    log.debug("E-mail marcado como lido");
                }
            }

            inbox.close(false);
            store.close();

        } catch (Exception e) {
            log.error("Erro crítico ao monitorar e-mails. Detalhes: ", e);
        }
    }

    private void processMessage(Message message) throws Exception {
        log.debug("Iniciando processamento do e-mail");

        if (message.getContent() instanceof Multipart multipart) {
            log.info("E-mail contém {} partes para análise", multipart.getCount());

            for (int i = 0; i < multipart.getCount(); i++) {
                BodyPart bodyPart = multipart.getBodyPart(i);
                log.debug("Analisando parte {} do e-mail", i + 1);

                if (bodyPart instanceof MimeBodyPart mimeBodyPart) {
                    log.debug("Tipo do conteúdo da parte {}: {}", i + 1, mimeBodyPart.getContentType());

                    if (isPdfAttachment(mimeBodyPart)) {
                        String fileName = mimeBodyPart.getFileName();
                        log.info("PDF encontrado: {}", fileName);

                        File tempFile = File.createTempFile("email-attachment-", ".pdf");
                        log.debug("Arquivo temporário criado: {}", tempFile.getAbsolutePath());

                        mimeBodyPart.saveFile(tempFile);
                        log.info("PDF salvo com sucesso no arquivo temporário");

                        try {
                            documentService.upload(tempFile);
                            log.info("PDF processado e enviado com sucesso: {}", fileName);
                        } catch (Exception e) {
                            log.error("Erro ao processar o PDF '{}'. Detalhes: ", fileName, e);
                            throw e;
                        }
                    }
                }
            }
        } else {
            log.warn("E-mail não contém estrutura multipart. Tipo de conteúdo: {}",
                    message.getContentType());
        }
        log.info("Verificação de e-mails concluída com sucesso");
    }

    private boolean isPdfAttachment(MimeBodyPart mimeBodyPart) throws Exception {
        boolean isPdf = mimeBodyPart.isMimeType("application/pdf") ||
                (mimeBodyPart.getFileName() != null &&
                        mimeBodyPart.getFileName().toLowerCase().endsWith(".pdf"));

        if (isPdf) {
            log.debug("Anexo identificado como PDF: {}", mimeBodyPart.getFileName());
        }

        return isPdf;
    }
}