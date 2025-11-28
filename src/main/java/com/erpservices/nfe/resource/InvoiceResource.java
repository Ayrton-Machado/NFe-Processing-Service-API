package com.erpservices.nfe.resource;

import com.erpservices.nfe.dto.InvoiceRequestDTO;
import com.erpservices.nfe.dto.InvoiceResponseDTO;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/invoices")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class InvoiceResource {
    
    @POST
    public Response createInvoice(InvoiceRequestDTO request) {
        InvoiceResponseDTO response = new InvoiceResponseDTO();
        response.message = "Invoice received for async processing.";
        response.trackingId = "12345-abcde-xyz";

        return Response.accepted(response).build();
    }
}
