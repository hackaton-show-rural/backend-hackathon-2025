package com.show_rural.hackathon.controller.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class DocumentFilters {
    private String search;
    private LocalDate date;

    public DocumentFilters() {
        search = search != null ? search.toLowerCase() : null;
    }

    public DocumentFilters(String search, LocalDate date) {
        this.search = search != null ? search.toLowerCase() : null;
        this.date = date;
    }
}
