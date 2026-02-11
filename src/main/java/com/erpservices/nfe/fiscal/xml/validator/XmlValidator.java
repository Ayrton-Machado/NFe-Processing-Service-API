package com.erpservices.nfe.fiscal.xml.validator;

import br.com.swconsultoria.nfe.Validar;
import br.com.swconsultoria.nfe.dom.enuns.ServicosEnum;
import jakarta.enterprise.context.ApplicationScoped;
import net.bytebuddy.implementation.bytecode.Throw;

@ApplicationScoped
public class XmlValidator {
    public void validate(String xml) {
        try {
            Validar validar = new Validar();

            Boolean valido = validar.isValidXml("src/main/resources/schemas", xml, ServicosEnum.ENVIO);

            if (!valido) {
                throw new RuntimeException("XML Inválido.");
            }

            System.out.println("XML válido.");
        } catch (Exception e) {
            throw new RuntimeException("Erro na validação: " + e.getMessage());
        }
    }    
}
