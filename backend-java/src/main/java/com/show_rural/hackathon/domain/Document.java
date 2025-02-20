package com.show_rural.hackathon.domain;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;


@Data
@Entity
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String cnpj;
    private String protocol;
    private String number;
    private String activity;
    private LocalDate limitDate;
    private DocumentStatus status;

    @OneToOne(cascade = CascadeType.ALL)
    private DocumentIdentifier identifier;

    @ElementCollection
    @Column(columnDefinition = "TEXT")
    private List<String> conditions;

    @Column(columnDefinition = "TEXT")
    private String documentUrl;

    @Column(columnDefinition = "boolean default false")
    private Boolean sentMail = false;
}
