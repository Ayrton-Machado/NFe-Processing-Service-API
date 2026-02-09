package com.erpservices.nfe.dto;

import java.util.List;

public class InvoiceRequestDTO {
    public String customerCpf;
    public String customerName;
    public String customerEmail;
    
    public List<InvoiceItemRequestDTO> items;
}
