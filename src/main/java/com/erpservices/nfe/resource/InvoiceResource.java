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

@Path("/nfe")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)

@ApplicationScoped
public class InvoiceResource {

    @Inject
    InvoiceService invoiceService;
    
    @POST
    @Path("/create")
    public Response createInvoice(InvoiceRequestDTO invoiceRequest) throws Exception {
        InvoiceResponseDTO response = invoiceService.issueInvoice(invoiceRequest);
        return Response.accepted(response).build();
    }

    @GET
    @Path("/list")
    public List<Invoice> listInvoices() {
        return Invoice.listAll();
    }

    @GET
    @Path("/tracking/{trackingId}")
    public Response getInvoiceByTrackingId(@PathParam("trackingId") String trackingId) {
        Invoice invoice = Invoice.find("trackingId", trackingId).firstResult();
        
        if (invoice == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ErrorResponse("Invoice not found with trackingId: " + trackingId))
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