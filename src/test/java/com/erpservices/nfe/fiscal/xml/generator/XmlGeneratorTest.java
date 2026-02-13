package com.erpservices.nfe.fiscal.xml.generator;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.Test;
import jakarta.inject.Inject;

import com.erpservices.nfe.fiscal.config.NfeConfigurator;
import com.erpservices.nfe.fiscal.xml.validator.XmlValidator;
import com.erpservices.nfe.model.Invoice;
import com.erpservices.nfe.model.InvoiceItem;

import br.com.swconsultoria.nfe.dom.ConfiguracoesNfe;
import br.com.swconsultoria.nfe.dom.enuns.EstadosEnum;
import br.com.swconsultoria.nfe.schema_4.enviNFe.TNFe;
import br.com.swconsultoria.nfe.util.XmlNfeUtil;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
public class XmlGeneratorTest {
    
    @Inject
    XmlGenerator xmlGenerator;

    @Inject
    XmlValidator xmlValidator;

    @ConfigProperty(name = "nfe.ambiente", defaultValue = "test")
    String ambiente;

    @Test
    public void testGenerateXml() throws Exception {
        // Criar invoice de teste (hardcoded - padrão para testes unitários)
        Invoice invoice = new Invoice();
        invoice.id = 1L; // ← Necessário para numeroNfe
        invoice.number = "20260213-000001";
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

        // Criar Configuração da NFE
        ConfiguracoesNfe config = NfeConfigurator.initConfigNfe(EstadosEnum.PR, ambiente);

        // Gera Objeto Nfe 
        TNFe nfe = xmlGenerator.generate(invoice, config);
        String xml = XmlNfeUtil.objectToXml(nfe);
        
        // NOTA: Validação XSD completa exige assinatura digital (certificado A1)
        xmlValidator.validate(xml);


        // Verificações básicas
        assert xml != null : "XML não deve ser nulo";
        assert xml.contains("<NFe") : "XML deve conter tag NFe";
        assert xml.contains("<infNFe") : "XML deve conter tag infNFe";
        assert xml.contains(invoice.customerCpf.replaceAll("[^0-9]", "")) : "XML deve conter CPF do cliente";
        
        System.out.println("XML gerado com sucesso!");
    }
}
