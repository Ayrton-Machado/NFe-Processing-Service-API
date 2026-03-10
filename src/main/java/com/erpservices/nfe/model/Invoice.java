package com.erpservices.nfe.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "invoices")
public class Invoice extends PanacheEntity {
    
    // ====== IDENTIFICAÇÃO ======
    @Column(name = "invoice_number", length = 50)
    public Long number;

    @Column(name = "issue_date")
    @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss")
    public LocalDateTime issueDate;
    
    @Column(nullable = false, length = 20)
    public String status; // RECEIVED, PROCESSING, COMPLETED, ERROR

    @Column(nullable = false)
    public String trackingId;
    
    // ====== VALOR PRINCIPAL ======
    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    public BigDecimal totalAmount;
    
    // ====== DESTINATÁRIO (MÍNIMO) ======
    @Column(name = "customer_cpf", nullable = false, length = 14)
    public String customerCpf;
    
    @Column(name = "customer_name", nullable = false, length = 120)
    public String customerName;

    @Column(name = "customer_email", nullable = false)
    public String customerEmail;
    
    // ======= ENDEREÇO DESTINATÁRIO =======
    @Column(name = "customer_street", nullable = false, length = 60)
    public String customerStreet;       // <xLgr> — logradouro. Ex: "Rua das Flores"

    @Column(name = "customer_number", nullable = false, length = 60)
    public String customerNumber;       // <nro> — número. Ex: "123" ou "S/N"

    @Column(name = "customer_district", nullable = false, length = 60)
    public String customerDistrict;     // <xBairro> — bairro. Ex: "Centro"

    @Column(name = "customer_city_code", nullable = false, length = 7)
    public String customerCityCode;     // <cMun> — código IBGE. Ex: "5219753"

    @Column(name = "customer_city", nullable = false, length = 60)
    public String customerCity;         // <xMun> — nome do município. Ex: "Goiânia"

    @Column(name = "customer_state", nullable = false, length = 2)
    public String customerState;        // <UF> — sigla do estado. Ex: "GO"

    @Column(name = "customer_zip_code", nullable = false, length = 8)
    public String customerZipCode;      // <CEP> — apenas números. Ex: "74000000"

    // ====== RELACIONAMENTOS ======
    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    public List<InvoiceItem> items;

    @OneToOne(mappedBy = "invoice", cascade = CascadeType.ALL)
    @JsonManagedReference("invoice-xml")
    public InvoiceXml invoiceXml;

    
    public InvoiceXml getInvoiceXml() {
        return invoiceXml;
    }

    public Long getNfeNumber() {
        return number;
    }

    public void setNumber(Long number) {
        this.number = number;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public void setCustomerEmail(String customerEmail) {
        this.customerEmail = customerEmail;
    }
}