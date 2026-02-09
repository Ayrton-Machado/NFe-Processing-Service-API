package com.erpservices.nfe.resource;

import java.util.List;

import com.erpservices.nfe.dto.InvoiceRequestDTO;
import com.erpservices.nfe.dto.InvoiceResponseDTO;
import com.erpservices.nfe.model.Invoice;
import com.erpservices.nfe.service.InvoiceService;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
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

    @GET
    public List<Invoice> listInvoices() {
        return Invoice.listAll();
    }

    @GET
    @Path("/{id}")
    public Response getInvoiceById(@PathParam("id") Long id) {
        Invoice invoice = Invoice.findById(id);
        
        if (invoice == null) {
            // Retorna JSON com mensagem de erro
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ErrorResponse("Invoice not found with id: " + id))
                    .build();
        }

        return Response.ok(invoice).build();
    }
    
    // Classe interna para erro
    public static class ErrorResponse {
        public String error;
        
        public ErrorResponse(String error) {
            this.error = error;
        }
    }
}