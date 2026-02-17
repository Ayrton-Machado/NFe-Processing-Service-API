package com.erpservices.nfe.service;

import java.io.File;

import io.quarkus.mailer.Mail;
import io.quarkus.mailer.Mailer;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class EmailService {
    @Inject
    Mailer mailer;
    
    /**
     * Envia DANFE por email para o cliente.
     * As credenciais SMTP são configuradas via variáveis de ambiente:
     * - SMTP_USERNAME
     * - SMTP_PASSWORD
     * 
     * @param destinatario Email do cliente
     * @param assunto Assunto do email
     * @param mensagem Corpo do email (HTML)
     * @param caminhoAnexo Caminho completo do PDF
     * @return true se enviado com sucesso
     */
    public boolean enviarEmailPdf(String destinatario, String assunto, String mensagem, String caminhoAnexo) {
        try {
            File anexo = new File(caminhoAnexo);
            String nomeArquivo = anexo.getName(); // Extrai "danfe-trackingId.pdf"

            mailer.send(
                Mail.withHtml(destinatario, assunto, mensagem)
                    .addAttachment(nomeArquivo, anexo, "application/pdf")
            );

            System.out.println("Arquivo encaminhado para seu E-mail: " + destinatario);

            return true;

        } catch (Exception e) {
            System.err.println("Erro ao enviar email (Exige variáveis de ambiente SMTP_USERNAME e SMTP_PASSWORD): " + e.getMessage());
            e.printStackTrace();

            return false;
        }
    }
}
