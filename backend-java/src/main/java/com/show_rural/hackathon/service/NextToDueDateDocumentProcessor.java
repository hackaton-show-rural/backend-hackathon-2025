package com.show_rural.hackathon.service;

import com.show_rural.hackathon.domain.Document;
import com.show_rural.hackathon.repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class NextToDueDateDocumentProcessor {

    private final static String MESSAGE = "";
    private final DocumentRepository documentRepository;
    private final EmailService emailService;

    @Scheduled(initialDelay = 0, fixedDelay = 10000)
    public void checkDocumentsExpiration() {
        Integer daysToNotify = 120;
        List<Document> documents = documentRepository.findNextExpirations(daysToNotify);

        if (!documents.isEmpty()) {
            log.info("Encontrados {} documentos próximos ao vencimento", documents.size());
        }

        for (var document : documents) {
            String subject = "Alerta de Vencimento - Documento " + document.getProtocol();

            String message = """
                    <!DOCTYPE html>
                    <html>
                    <head>
                        <meta charset="UTF-8">
                        <style>
                            body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                            .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                            .header { background-color: #f8f9fa; padding: 20px; text-align: center; border-radius: 5px; }
                            .content { padding: 20px; background-color: #ffffff; }
                            .warning { color: #dc3545; font-weight: bold; }
                            .info { margin: 20px 0; padding: 15px; background-color: #f8f9fa; border-radius: 5px; }
                            .footer { text-align: center; margin-top: 20px; font-size: 12px; color: #6c757d; }
                            .button { display: inline-block; padding: 10px 20px; background-color: #007bff; color: #ffffff; 
                                     text-decoration: none; border-radius: 5px; margin-top: 20px; }
                        </style>
                    </head>
                    <body>
                        <div class="container">
                            <div class="header">
                                <h2>Alerta de Vencimento de Documento</h2>
                            </div>
                            <div class="content">
                                <p>Prezado(a),</p>
                                
                                <p>Identificamos que um documento importante está próximo da data de vencimento:</p>
                                
                                <div class="info">
                                    <p><strong>Protocolo:</strong> %s</p>
                                    <p><strong>Nome:</strong> %s</p>
                                    <p><strong>CNPJ:</strong> %s</p>
                                    <p><strong>Número:</strong> %s</p>
                                    <p class="warning"><strong>Data de Vencimento:</strong> %s</p>
                                </div>
                                
                                <p>Por favor, tome as providências necessárias para renovação do documento antes do vencimento.</p>
                                
                                <a href="%s" class="button">Visualizar Documento</a>
                                
                                <p>Se precisar de ajuda, entre em contato com nossa equipe de suporte.</p>
                                
                                <p>Atenciosamente,<br>Equipe de Gestão de Documentos</p>
                            </div>
                            <div class="footer">
                                <p>Este é um e-mail automático, por favor não responda.</p>
                            </div>
                        </div>
                    </body>
                    </html>
                    """.formatted(
                    document.getProtocol(),
                    document.getIdentifier().getName(),
                    document.getCnpj(),
                    document.getNumber(),
                    document.getLimitDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                    document.getDocumentUrl()
            );

            try {
                emailService.send(subject, message);
                log.info("Email de alerta enviado com sucesso para documento: {}", document.getProtocol());
            } catch (Exception e) {
                log.error("Erro ao enviar email de alerta para documento: {}", document.getProtocol(), e);
            }
        }
    }
}
