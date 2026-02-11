package com.erpservices.nfe.fiscal.xml.generator;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import jakarta.inject.Inject;

import com.erpservices.nfe.fiscal.xml.validator.XmlValidator;
import com.erpservices.nfe.model.Invoice;
import com.erpservices.nfe.model.InvoiceItem;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
public class XmlGeneratorTest {
    
    @Inject
    XmlGenerator xmlGenerator;

    @Inject
    XmlValidator xmlValidator;

    @Test
    public void testGenerateXml() {
        // Criar invoice de teste
        Invoice invoice = new Invoice();
        invoice.invoiceNumber = "000000001";
        invoice.issueDate = LocalDateTime.now();
        invoice.customerCpf = "12345678901";
        invoice.customerName = "Cliente Teste";
        invoice.customerEmail = "cliente@teste.com";
        invoice.totalAmount = new BigDecimal("100.00");
        
        // Adicionar item
        InvoiceItem item = new InvoiceItem();
        item.productCode = "PROD001";
        item.quantity = 1;
        item.unitPrice = new BigDecimal("100.00");
        item.totalPrice = new BigDecimal("100.00");
        invoice.items = List.of(item);
        
        // Gerar XML
        String xml = xmlGenerator.generate(invoice);

        // Validar XML
        xmlValidator.validate(xml);
        
        // Imprimir XML
        System.out.println("=== XML GERADO ===");
        System.out.println(xml);
        
        // Verificações básicas
        assert xml.contains("<NFe");
        assert xml.contains("<nNF>000000001</nNF>");
    }
}
