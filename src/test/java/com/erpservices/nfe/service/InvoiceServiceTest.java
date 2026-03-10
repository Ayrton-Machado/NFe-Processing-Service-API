package com.erpservices.nfe.service;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;
import jakarta.inject.Inject;

import com.erpservices.nfe.dto.InvoiceItemRequestDTO;
import com.erpservices.nfe.dto.InvoiceRequestDTO;
import com.erpservices.nfe.dto.InvoiceResponseDTO;

import static org.junit.jupiter.api.Assertions.*;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
public class InvoiceServiceTest {

    @Inject
    InvoiceService invoiceService;

    /**
     * Testa o fluxo completo de emissão de NF-e via InvoiceService.
     * Ao testar pelo service (contrato público), mudanças internas
     * nos componentes não quebram este teste.
     */
    @Test
    public void testIssueInvoice() throws Exception {
        // Monta DTO de requisição
        InvoiceRequestDTO request = new InvoiceRequestDTO();
        request.customerCpf = "12345678901";
        request.customerName = "Cliente Teste";
        request.customerEmail = "cliente@teste.com";
        request.customerState = "SP";
        request.customerStreet = "Rua Teste";
        request.customerNumber = "100";
        request.customerDistrict = "Bairro Teste";
        request.customerCity = "São Paulo";
        request.customerCityCode = "3550308";
        request.customerZipCode = "01001000";

        InvoiceItemRequestDTO item = new InvoiceItemRequestDTO();
        item.productCode = "PROD001";
        item.quantity = 1;
        item.unitPrice = new BigDecimal("100.00");
        request.items = List.of(item);

        // Executa o fluxo completo
        InvoiceResponseDTO response = invoiceService.issueInvoice(request);

        // Verificações no contrato de resposta
        assertNotNull(response, "Response não deve ser nula");
        assertNotNull(response.trackingId, "TrackingId não deve ser nulo");
        assertNotNull(response.issueDate, "IssueDate não deve ser nula");
        assertEquals("Pedido Confirmado", response.message, "Mensagem de confirmação incorreta");
    }
}
