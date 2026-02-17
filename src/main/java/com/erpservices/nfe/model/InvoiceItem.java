package com.erpservices.nfe.model;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonBackReference;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

import jakarta.persistence.*;

@Entity
@Table(name = "invoice_items")
public class InvoiceItem extends PanacheEntity {
    
    @Column(name = "product_code", nullable = false, length = 50)
    public String productCode;
    
    @Column(nullable = false)
    public Integer quantity;
    
    @Column(name = "unit_price", nullable = false, precision = 10, scale = 2)
    public BigDecimal unitPrice;
    
    @Column(name = "total_price", nullable = false, precision = 10, scale = 2)
    public BigDecimal totalPrice;  // quantity × unitPrice (SEM desconto)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id", nullable = false)
    @JsonBackReference
    public Invoice invoice;
    
}