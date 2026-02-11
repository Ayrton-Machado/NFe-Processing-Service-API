package com.erpservices.nfe.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;


import com.erpservices.nfe.dto.InvoiceItemRequestDTO;
import com.erpservices.nfe.dto.InvoiceRequestDTO;
import com.erpservices.nfe.dto.InvoiceResponseDTO;
import com.erpservices.nfe.fiscal.xml.generator.XmlGenerator;
import com.erpservices.nfe.fiscal.xml.validator.XmlValidator;
import com.erpservices.nfe.model.Invoice;
import com.erpservices.nfe.model.InvoiceItem;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class InvoiceService {

    @Inject
    XmlGenerator xmlGenerator;

    @Inject 
    XmlValidator xmlValidator;

    private static final AtomicLong invoiceCounter = new AtomicLong(1);

    @Transactional
    public InvoiceResponseDTO processInvoice(InvoiceRequestDTO invoiceRequest) {

        // Gera dados de controle
        String trackingId = UUID.randomUUID().toString();
        String invoiceNumber = generateInvoiceNumber();
        LocalDateTime issueDate = LocalDateTime.now();
        
        // Cria entidade Invoice
        Invoice invoice = new Invoice();
        invoice.invoiceNumber = invoiceNumber;
        invoice.issueDate = issueDate;
        invoice.status = "RECEIVED";
        invoice.customerCpf = invoiceRequest.customerCpf;
        invoice.customerName = invoiceRequest.customerName;
        invoice.customerEmail = invoiceRequest.customerEmail;
        
        // Cria items
        BigDecimal totalAmount = BigDecimal.ZERO;
        List<InvoiceItem> items = new ArrayList<>();
        for (InvoiceItemRequestDTO itemDTO : invoiceRequest.items) {
            InvoiceItem item = new InvoiceItem();
            item.productCode = itemDTO.productCode;
            item.quantity = itemDTO.quantity;
            item.unitPrice = itemDTO.unitPrice;

            BigDecimal itemTotal = item.unitPrice.multiply(BigDecimal.valueOf(item.quantity));
            item.totalPrice = itemTotal;
            totalAmount =  totalAmount.add(itemTotal);

            item.invoice = invoice;
            items.add(item);
        }
        invoice.items = items;
        invoice.totalAmount = totalAmount;
        
        // Persiste no banco
        invoice.persist();

        // Gera Xml
        String xml = xmlGenerator.generate(invoice);

        // Valida Estrutura Xml com .xsd
        xmlValidator.validate(xml);
        
        // Monta resposta para o cliente
        InvoiceResponseDTO response = new InvoiceResponseDTO();
        response.trackingId = trackingId;
        response.issueDate = issueDate;
        response.message = "Invoice received for async processing";
        
        return response;
    }
    
    private String generateInvoiceNumber() {
        LocalDateTime now = LocalDateTime.now();
        String datePart = now.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        long sequence = invoiceCounter.getAndIncrement();
        
        // Formato: YYYYMMDD-NNNNNN (ex: 20260205-000001)
        return String.format("%s-%06d", datePart, sequence);
    }
}