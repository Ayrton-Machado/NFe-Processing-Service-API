package com.erpservices.nfe.fiscal.config;

import br.com.swconsultoria.certificado.Certificado;
import br.com.swconsultoria.nfe.dom.ConfiguracoesNfe;
import br.com.swconsultoria.nfe.dom.enuns.AmbienteEnum;
import br.com.swconsultoria.nfe.dom.enuns.EstadosEnum;

import java.math.BigInteger;
import java.time.LocalDate;

public class NfeConfigurator {
    
    /**
     * ATENÇÃO: Este certificado é MOCK para desenvolvimento local apenas.
     * NÃO funcionará para enviar à SEFAZ (homologação ou produção).
     * Uso apenas para gerar e validar XML localmente.
     */
    public static ConfiguracoesNfe initConfigNfe(EstadosEnum estado, AmbienteEnum ambiente) throws Exception {
        // Certificado mock para desenvolvimento
        Certificado certificado = createMockCertificado();
        
        return ConfiguracoesNfe.criarConfiguracoes(estado, ambiente, certificado, "src/main/resources/schemas");
    }
    
    private static Certificado createMockCertificado() {
        Certificado cert = new Certificado();
        cert.setNome("Certificado Mock - Desenvolvimento");
        cert.setVencimento(LocalDate.now().plusYears(1)); // Válido por 1 ano
        cert.setDiasRestantes(365L);
        cert.setCnpjCpf("12345678000195"); // CNPJ fake
        cert.setValido(true); // Marca como válido
        cert.setNumeroSerie(BigInteger.valueOf(123456789L));
        cert.setModoMultithreading(false);
        return cert;
    }
}
