package com.erpservices.nfe.model;

import java.math.BigDecimal;
import java.time.LocalDate;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Entity;

import jakarta.persistence.OneToMany;
import jakarta.persistence.CascadeType;
import java.util.List;

@Entity
public class Invoice extends PanacheEntity {
    public String InvoiceNumber;
    public LocalDate issueDate;
    public String supplierCnpj;
    public BigDecimal totalAmount;

    public String trackingId;
    public String status;

    @OneToMany(mappedBy = "Invoice", cascade = CascadeType.ALL, orphanRemoval = true)
    public List<InvoiceItem> items;
}