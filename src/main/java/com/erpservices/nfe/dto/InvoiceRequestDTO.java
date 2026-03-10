package com.erpservices.nfe.dto;

import java.util.List;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

public class InvoiceRequestDTO {

    // ====== DESTINATÁRIO ======
    @Schema(description = "CPF do cliente (apenas números)", defaultValue = "12345678901", required = true)
    public String customerCpf;

    @Schema(description = "Nome completo do cliente", defaultValue = "João da Silva", required = true)
    public String customerName;

    @Schema(description = "Email do cliente", defaultValue = "cliente@email.com", required = true)
    public String customerEmail;

    // ====== ENDEREÇO DESTINATÁRIO ======
    @Schema(description = "Logradouro", defaultValue = "Rua das Flores", required = true)
    public String customerStreet;

    @Schema(description = "Número do endereço", defaultValue = "123", required = true)
    public String customerNumber;

    @Schema(description = "Bairro", defaultValue = "Centro", required = true)
    public String customerDistrict;

    @Schema(description = "Código IBGE do município (7 dígitos)", defaultValue = "5219753", required = true)
    public String customerCityCode;

    @Schema(description = "Nome do município", defaultValue = "Goiânia", required = true)
    public String customerCity;

    @Schema(description = "Sigla do estado (UF)", defaultValue = "GO", required = true)
    public String customerState;

    @Schema(description = "CEP (apenas números)", defaultValue = "74000000", required = true)
    public String customerZipCode;

    // ====== ITENS ======
    @Schema(description = "Lista de itens da nota fiscal", required = true)
    public List<InvoiceItemRequestDTO> items;
}
