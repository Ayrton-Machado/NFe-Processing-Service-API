package com.erpservices.nfe.model;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonBackReference;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "invoice_xmls")
public class InvoiceXml extends PanacheEntity {

    // ====== RELACIONAMENTO ======
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id", nullable = false, unique = true)
    @JsonBackReference("invoice-xml")
    public Invoice invoice;

    // ====== IDENTIFICAÇÃO FISCAL ======
    @Column(name = "chave_acesso", length = 44, unique = true)
    public String chaveAcesso;

    // ====== S3 ======
    @Column(name = "xml_s3_key")
    public String xmlS3Key;

    // ====== CONTROLE ======
    @Column(name = "uploaded_at")
    public LocalDateTime uploadedAt;

    @Column(name = "sefaz_status")
    public String sefazStatus; // AUTORIZADO, REJEITADO, PENDENTE

    public String getChaveAcesso() {
        return chaveAcesso;
    }

    
}
