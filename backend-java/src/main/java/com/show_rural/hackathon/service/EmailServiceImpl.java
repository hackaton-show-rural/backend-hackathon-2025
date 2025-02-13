package com.show_rural.hackathon.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final static String RECEIVER = "contato@folhastech.com";
    private final JavaMailSender mailSender;

    public void send(String subject, String message) {
        log.info("Iniciando envio de email para: {}", RECEIVER);

        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "UTF-8");

            helper.setFrom("contato@folhastech.com");
            helper.setTo(RECEIVER);
            helper.setSubject(subject);
            helper.setText(message, true);

            mailSender.send(mimeMessage);
            log.info("Email enviado com sucesso para: {}", RECEIVER);

        } catch (MessagingException e) {
            log.error("Erro ao enviar email para: {}. Erro: {}", RECEIVER, e.getMessage(), e);
            throw new RuntimeException("Falha ao enviar email", e);
        }
    }
}
