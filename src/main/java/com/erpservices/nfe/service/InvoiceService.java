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
import com.erpservices.nfe.fiscal.impressao.DanfeService;
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

    @Inject
    NfeConfigurator nfeConfigurator;

    @Inject
    DanfeService danfeService;

    @Inject
    EmailService emailService;

    @ConfigProperty(name = "nfe.ambiente", defaultValue = "test")
    String ambiente;

    
    @ConfigProperty(name = "nfe.emitente.razao-social", defaultValue = "EMPRESA GRANDE LTDA")
    String emitenteRazaoSocial;
    

    private static final AtomicLong invoiceCounter = new AtomicLong(1);

    /**
     * Emite uma nota fiscal eletrônica (NFe).
     * Orquestra o fluxo da NFe: Preenche objeto NFe -> Processa NFe (Gera e Valida XML) -> Gera DANFE -> Enviar por email.
     * 
     * @param invoiceRequest DTO com os dados da requisição da nota fiscal
     * @return InvoiceResponseDTO com dados de rastreamento e confirmação de emissão
     */
    @Transactional
    public InvoiceResponseDTO issueInvoice(InvoiceRequestDTO invoiceRequest) throws Exception {
        // 1. Cria e persiste a Invoice
        Invoice invoice = preencheInvoice(invoiceRequest);

        
        // 2. Processa (gera XML, valida e envia)
        String xml = processInvoice(invoice);
        
        // 3. Gera DANFE
        String caminhoArquivo = danfeService.gerarDanfe(xml, "danfe" + invoice.trackingId);

        // Monta resposta para o cliente
        InvoiceResponseDTO response = new InvoiceResponseDTO();
        response.trackingId = invoice.trackingId;
        response.issueDate = invoice.issueDate;
        response.message = "Pedido Confirmado";
        
        // 4. Envia Email
        enviarDanfe(invoice, caminhoArquivo);

        return response;
    }

    private void enviarDanfe(Invoice invoice, String caminhoArquivo) {
        // 4. Enviar DANFE por email
        String mensagem = String.format(
            "<h2>Nota Fiscal Eletrônica</h2>" +
            "<p>Prezado(a) %s,</p>" +
            "<p>Segue em anexo o DANFE da NFe nº %s.</p>",
            invoice.getCustomerName(),
            invoice.getNfeNumber()
        );

        emailService.enviarEmailPdf(
            invoice.getCustomerEmail(),
            "NFe " + invoice.getNfeNumber() + " - " + emitenteRazaoSocial,
            mensagem,
            caminhoArquivo
        );
    }

    private String processInvoice(Invoice invoice) throws Exception {
        // Criar Configuração da NFE
        ConfiguracoesNfe config = nfeConfigurator.initConfigNfe(EstadosEnum.PR, ambiente);

        // Gera Objeto Nfe
        TNFe nfe = xmlGenerator.generate(invoice, config);
        String xml = XmlNfeUtil.objectToXml(nfe);

        // Exibe XML em log
        System.out.println("=== XML GERADO ===");
        System.out.println(xml);
        System.out.println("================== \n");

        // Valida Estrutura Xml com .xsd 
        xmlValidator.validate(xml);
        
        // Ambiente PROD realizará envio com valor fiscal real
        if (ambiente.equals("prod") || ambiente.equals("homolog")) {
            // Envia NFE para Sefaz através do webservice (Envio exige certificado A1) 
            // NÃO-TESTADO
            sendNfe.send(nfe, config);
        }

        return xml;
    }

    private Invoice preencheInvoice(InvoiceRequestDTO invoiceRequest) throws Exception {
        // Gera dados de controle
        String invoiceNumber = generateInvoiceNumber();
        LocalDateTime issueDate = LocalDateTime.now();
        String trackingId = UUID.randomUUID().toString();

        // Cria entidade Invoice
        Invoice invoice = new Invoice();
        invoice.number = invoiceNumber;
        invoice.issueDate = issueDate;
        invoice.status = "RECEIVED";
        invoice.customerCpf = invoiceRequest.customerCpf;
        invoice.customerName = invoiceRequest.customerName;
        invoice.customerEmail = invoiceRequest.customerEmail;
        invoice.trackingId = trackingId;

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