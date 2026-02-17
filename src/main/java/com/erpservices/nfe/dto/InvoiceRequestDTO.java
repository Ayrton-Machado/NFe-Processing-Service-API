package com.erpservices.nfe.dto;

import java.util.List;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

public class InvoiceRequestDTO {
    public String customerCpf;
    public String customerName;
    
    @Schema(description = "Email do cliente", defaultValue = "cliente@email.com", required = true)
    public String customerEmail;
    
    public List<InvoiceItemRequestDTO> items;
}
