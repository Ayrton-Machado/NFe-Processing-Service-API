package com.erpservices.nfe.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

import com.erpservices.nfe.dto.InvoiceItemRequestDTO;
import com.erpservices.nfe.dto.InvoiceRequestDTO;
import com.erpservices.nfe.dto.InvoiceResponseDTO;
import com.erpservices.nfe.fiscal.config.NfeConfigurator;
import com.erpservices.nfe.fiscal.envio.SendNfe;
import com.erpservices.nfe.fiscal.xml.generator.XmlGenerator;
import com.erpservices.nfe.fiscal.xml.validator.XmlValidator;
import com.erpservices.nfe.model.Invoice;
import com.erpservices.nfe.model.InvoiceItem;

import br.com.swconsultoria.nfe.dom.ConfiguracoesNfe;
import br.com.swconsultoria.nfe.dom.enuns.EstadosEnum;
import br.com.swconsultoria.nfe.schema_4.enviNFe.TNFe;
import br.com.swconsultoria.nfe.util.XmlNfeUtil;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class InvoiceService {

    @Inject
    XmlGenerator xmlGenerator;

    @Inject
    XmlValidator xmlValidator;

    @Inject
    SendNfe sendNfe;

    @ConfigProperty(name = "nfe.ambiente", defaultValue = "test")
    String ambiente;

    private static final AtomicLong invoiceCounter = new AtomicLong(1);

    /**
     * Emite uma nota fiscal eletrônica (NFe).
     * Método principal que cria a invoice, gera o XML, valida e envia para a SEFAZ.
     * 
     * @param invoiceRequest DTO com os dados da requisição da nota fiscal
     * @return InvoiceResponseDTO com dados de rastreamento e confirmação de emissão
     */
    @Transactional
    public InvoiceResponseDTO issueInvoice(InvoiceRequestDTO invoiceRequest) throws Exception {
        // 1. Cria e persiste a Invoice
        Invoice invoice = preencheInvoice(invoiceRequest);
        
        // 2. Processa (gera XML, valida e envia)
        InvoiceResponseDTO response = processInvoice(invoice);
        
        return response;
    }

    private InvoiceResponseDTO processInvoice(Invoice invoice) throws Exception {
        // Geração de dados de controle
        String trackingId = UUID.randomUUID().toString();

        // Criar Configuração da NFE
        ConfiguracoesNfe config = NfeConfigurator.initConfigNfe(EstadosEnum.PR, ambiente);

        // Gera Objeto Nfe
        TNFe nfe = xmlGenerator.generate(invoice, config);
        String xml = XmlNfeUtil.objectToXml(nfe);

        // TODO: Deve enviar email após resposta do sistema
        
        // Debug: Salvar XML em arquivo para inspeção
        System.out.println("=== XML GERADO ===");
        System.out.println(xml);
        System.out.println("==================");

        // Valida Estrutura Xml com .xsd 
        xmlValidator.validate(xml);
        
        if (ambiente.equals("prod") || ambiente.equals("homolog")) {
            // Envia NFE para Sefaz através do webservice 
            sendNfe.send(nfe, config); 
        } else {
            // TODO: Simula envio para o SEFAZ
            sendNfe.sendNfeMocked(nfe, config);
        }

        // Monta resposta para o cliente
        InvoiceResponseDTO response = new InvoiceResponseDTO();
        response.trackingId = trackingId;
        response.issueDate = invoice.issueDate;
        response.message = "Invoice received for async processing";

        return response;
    }

    private Invoice preencheInvoice(InvoiceRequestDTO invoiceRequest) throws Exception {
        // Gera dados de controle
        String invoiceNumber = generateInvoiceNumber();
        LocalDateTime issueDate = LocalDateTime.now();

        // Cria entidade Invoice
        Invoice invoice = new Invoice();
        invoice.number = invoiceNumber;
        invoice.issueDate = issueDate;
        invoice.status = "RECEIVED";
        invoice.customerCpf = invoiceRequest.customerCpf;
        invoice.customerName = invoiceRequest.customerName;
        invoice.customerEmail = invoiceRequest.customerEmail;

        // Cria items
        BigDecimal totalAmount = BigDecimal.ZERO;
        List<InvoiceItem> items = new ArrayList<>();
        for (InvoiceItemRequestDTO itemDTO : invoiceRequest.items) {
            InvoiceItem item = new InvoiceItem();
            item.productCode = itemDTO.productCode;
            item.quantity = itemDTO.quantity;
            item.unitPrice = itemDTO.unitPrice;

            BigDecimal itemTotal = item.unitPrice.multiply(BigDecimal.valueOf(item.quantity));
            item.totalPrice = itemTotal;
            totalAmount = totalAmount.add(itemTotal);

            item.invoice = invoice;
            items.add(item);
        }
        invoice.items = items;
        invoice.totalAmount = totalAmount;

        // Persiste no banco
        invoice.persist();

        // Retorna NFE crua
        return invoice;
    }

    private String generateInvoiceNumber() {
        LocalDateTime now = LocalDateTime.now();
        String datePart = now.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        long sequence = invoiceCounter.getAndIncrement();

        // Formato: YYYYMMDD-NNNNNN (ex: 20260205-000001)
        return String.format("%s-%06d", datePart, sequence);
    }
}