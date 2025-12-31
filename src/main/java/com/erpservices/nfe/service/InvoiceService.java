package com.erpservices.nfe.service;

import java.util.UUID;

import com.erpservices.nfe.dto.InvoiceRequestDTO;
import com.erpservices.nfe.dto.InvoiceResponseDTO;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class InvoiceService {

    @Transactional
    public InvoiceResponseDTO processInvoice(InvoiceRequestDTO invoiceRequest) {

        String trackingId = UUID.randomUUID().toString();
        InvoiceResponseDTO response = new InvoiceResponseDTO();
        response.message = "Invoice received for async processing";
        response.trackingId = trackingId;
        
        return response;
    }

}