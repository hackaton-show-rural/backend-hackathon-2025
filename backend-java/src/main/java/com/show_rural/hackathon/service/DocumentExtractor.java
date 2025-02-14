package com.show_rural.hackathon.service;

import com.show_rural.hackathon.domain.Document;
import com.show_rural.hackathon.domain.DocumentIdentifier;
import com.show_rural.hackathon.util.DocGenerator;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class DocumentExtractor {
    public Document processFile(MultipartFile file) {
        try {
            if (file.isEmpty()) {
                throw new IllegalArgumentException("Arquivo está vazio");
            }

            if (!file.getContentType().equals("application/pdf")) {
                throw new IllegalArgumentException("Arquivo deve ser um PDF");
            }


            PDDocument pdDocument = PDDocument.load(file.getInputStream());
            PDFTextStripper pdfStripper = new PDFTextStripper();
            String content = pdfStripper.getText(pdDocument);
            pdDocument.close();

            return extract(content);

        } catch (IOException e) {
            throw new RuntimeException("Erro ao processar arquivo PDF: " + e.getMessage(), e);
        }
    }

    public Document processFile(File file) {
        try {
            if (!file.exists()) {
                throw new IllegalArgumentException("Arquivo está vazio");
            }

            PDDocument pdDocument = PDDocument.load(new FileInputStream(file));
            PDFTextStripper pdfStripper = new PDFTextStripper();
            String content = pdfStripper.getText(pdDocument);
            pdDocument.close();

            return extract(content);

        } catch (IOException e) {
            throw new RuntimeException("Erro ao processar arquivo PDF: " + e.getMessage(), e);
        }
    }

    private Document extract(String content) {
        content = content.replaceAll("EM BRANCO", "");
        Document document = new Document();
        document.setCnpj(DocGenerator.generateDoc());

        Pattern protocolPattern = Pattern.compile("Número do Protocolo\\s*\\n+Número do Documento\\s*\\n+(\\d{2}\\.\\d{3}\\.\\d{3}-\\d)");
        Matcher protocolMatcher = protocolPattern.matcher(content);
        if (protocolMatcher.find()) {
            document.setProtocol(protocolMatcher.group(1));
        }

        Pattern documentNumberPattern = Pattern.compile("Número do Documento\\s*\\n+.*?\\n+(\\d{6}(?:-R\\d)?)", Pattern.DOTALL);
        Matcher documentNumberMatcher = documentNumberPattern.matcher(content);
        if (documentNumberMatcher.find()) {
            document.setNumber(documentNumberMatcher.group(1));
        }

        Pattern dateLimitPattern = Pattern.compile("Validade da Licença[\\s\\n]+(\\d{2}/\\d{2}/\\d{4})");
        Matcher dateLimitMatcher = dateLimitPattern.matcher(content);
        if (dateLimitMatcher.find()) {
            String dataStr = dateLimitMatcher.group(1);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            document.setLimitDate(LocalDate.parse(dataStr, formatter));
        }

        DocumentIdentifier identifier = new DocumentIdentifier();

        Pattern namePattern = Pattern.compile("1\\.\\s*IDENTIFICAÇÃO\\s*DO\\s*EMPREENDEDOR\\s*\\n(.*?)\\s*\\n");
        Matcher nameMatcher = namePattern.matcher(content);
        if (nameMatcher.find()) {
            String name = nameMatcher.group(1).trim();
            name = name.substring(name.indexOf("-**") + 3);
            identifier.setName(name);
        }

        Pattern addressPattern = Pattern.compile("([^\\n]+)\\s+(\\d{2}\\.\\d{3}-\\d{3})([^\\n]+)");
        Matcher addressMatcher = addressPattern.matcher(content);
        if (addressMatcher.find()) {
            identifier.setAddress(addressMatcher.group(1).trim());
            identifier.setPostalCode(addressMatcher.group(2));
            identifier.setCity(addressMatcher.group(3).replace("/PR", "").trim());
        }

        document.setIdentifier(identifier);

        List<String> conditions = new ArrayList<>();
        String conditionants = "CONDICIONANTES";
        int startIndex = content.indexOf(conditionants) + conditionants.length();

        Pattern conditionPattern =
                Pattern.compile("(\\d+)\\.\\s+(\\D[^\\n]*(?:\\n(?!\\d+\\.)[^\\n]*)*)", Pattern.MULTILINE);
        Matcher matcher = conditionPattern.matcher(content.substring(startIndex));

        while (matcher.find()) {
            String conditionNumber = matcher.group(1);
            String conditionText = matcher.group(2).replaceAll("\\s+", " ").trim();
            conditions.add(String.format("%s. %s", conditionNumber, conditionText));
        }
        document.setConditions(conditions);

        return document;
    }
}
