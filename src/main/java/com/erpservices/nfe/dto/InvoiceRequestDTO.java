package com.erpservices.nfe.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class InvoiceRequestDTO {
    public String invoiceNumber;
    public LocalDate issueDate;
    public String supplierCnpj;
    public BigDecimal totalAmount;

    public List<InvoiceItemRequestDTO> items;
}
