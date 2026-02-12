package com.erpservices.nfe.fiscal.xml.validator;

import jakarta.enterprise.context.ApplicationScoped;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.File;
import java.io.StringReader;

@ApplicationScoped
public class XmlValidator {
    
    private static final String SCHEMA_PATH = "src/main/resources/schemas";
    
    public void validate(String xml) {
        try {
            // Cria a fábrica de schemas (padrão W3C = formato XSD comum)
            SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            
            // Carrega o arquivo XSD
            File schemaFile = new File(SCHEMA_PATH + "/nfe_v4.00.xsd");
            Schema schema = schemaFactory.newSchema(schemaFile);
            
            // Cria o validador
            Validator validator = schema.newValidator();
            
            // Valida o XML - se tiver erro, lança exceção SAXException automaticamente
            validator.validate(new StreamSource(new StringReader(xml)));
            
            System.out.println("XML válido!");
            
        } catch (SAXException e) {
            // Aqui cai quando o XML é inválido - mostra o erro cru
            throw new RuntimeException("XML inválido: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException("Erro na validação: " + e.getMessage(), e);
        }
    }
}
