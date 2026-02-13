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
    public String number;
    
    @Column(name = "issue_date")
    @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss")
    public LocalDateTime issueDate;
    
    @Column(nullable = false, length = 20)
    public String status; // RECEIVED, PROCESSING, COMPLETED, ERROR
    
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
    
    // ====== RELACIONAMENTOS ======
    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    public List<InvoiceItem> items;

}