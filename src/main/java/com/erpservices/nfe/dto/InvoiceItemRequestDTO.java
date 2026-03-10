package com.erpservices.nfe.dto;

import java.math.BigDecimal;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

public class InvoiceItemRequestDTO {

    @Schema(description = "Código do produto", defaultValue = "PROD001", required = true)
    public String productCode;

    @Schema(description = "Quantidade", defaultValue = "2", required = true)
    public int quantity;

    @Schema(description = "Valor unitário", defaultValue = "150.00", required = true)
    public BigDecimal unitPrice;
}
