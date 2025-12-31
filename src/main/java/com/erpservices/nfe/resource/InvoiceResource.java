package com.erpservices.nfe.resource;

import com.erpservices.nfe.dto.InvoiceRequestDTO;
import com.erpservices.nfe.dto.InvoiceResponseDTO;
import com.erpservices.nfe.service.InvoiceService;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/invoices")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)

@ApplicationScoped
public class InvoiceResource {

    @Inject
    InvoiceService invoiceService;
    
    @POST
    public Response createInvoice(InvoiceRequestDTO invoiceRequest) {
        InvoiceResponseDTO response = invoiceService.processInvoice(invoiceRequest);

        return Response.accepted(response).build();
    }
}