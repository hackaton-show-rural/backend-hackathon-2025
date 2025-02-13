package com.show_rural.hackathon.domain;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;


@Data
@Table(indexes = {@Index(columnList = "documentId")})
@Entity
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String protocol;
    private String number;
    private LocalDate limitDate;

    @OneToOne(cascade = CascadeType.ALL)
    private DocumentIdentifier identifier;

    @ElementCollection
    @Column(columnDefinition = "TEXT")
    private List<String> conditions;

    @Column(nullable = false)
    private String documentId;
}
