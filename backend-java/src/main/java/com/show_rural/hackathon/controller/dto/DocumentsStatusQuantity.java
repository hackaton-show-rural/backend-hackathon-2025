package com.show_rural.hackathon.controller.dto;

import com.show_rural.hackathon.domain.DocumentStatus;
import lombok.Data;

@Data
public class DocumentsStatusQuantity {
    private DocumentStatus status;
    private Long quantity;
}
