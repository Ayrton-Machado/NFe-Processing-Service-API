package com.erpservices.nfe.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

public class InvoiceResponseDTO {
    public String message;

    @JsonFormat(pattern = "dd/MM/yyyy HH:mm")
    public LocalDateTime issueDate;
    
    public String trackingId;
}