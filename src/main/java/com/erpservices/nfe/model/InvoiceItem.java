package com.erpservices.nfe.model;

import java.math.BigDecimal;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;

@Entity
public class InvoiceItem extends PanacheEntity {
    public String productCode;
    public int quantity;
    public BigDecimal unitPrice;

    @ManyToOne
    public Invoice Invoice;
}
