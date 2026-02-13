package com.erpservices.nfe.fiscal.config;

import br.com.swconsultoria.certificado.Certificado;
import br.com.swconsultoria.certificado.CertificadoService;
import br.com.swconsultoria.nfe.dom.ConfiguracoesNfe;
import br.com.swconsultoria.nfe.dom.enuns.AmbienteEnum;
import br.com.swconsultoria.nfe.dom.enuns.EstadosEnum;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.math.BigInteger;
import java.time.LocalDate;

public class NfeConfigurator {

    @ConfigProperty(name = "nfe.ambiente", defaultValue = "test")
    String ambiente;

    /**
     * Converte string do properties para AmbienteEnum
     */
    public static AmbienteEnum getAmbiente(String ambiente) {
        return switch (ambiente) {
            case "prod" -> AmbienteEnum.PRODUCAO;
            case "homolog" -> AmbienteEnum.HOMOLOGACAO;
            default -> AmbienteEnum.HOMOLOGACAO; // test usa homolog com mock
        };
    }

    /**
     * ATENÇÃO: Certificado mock usado apenas em teste.
     * Para produção e homologacao, configure certificado real via variáveis de ambiente.
     */
    public static ConfiguracoesNfe initConfigNfe(EstadosEnum estado, String ambienteStr) throws Exception {
        AmbienteEnum ambiente = getAmbiente(ambienteStr);
        Certificado certificado;
        
        if (ambienteStr.equals("prod") || ambienteStr.equals("homolog")) {
            // Lê diretamente das variáveis de ambiente (não expõe em logs/parâmetros)
            String certPath = System.getenv("NFE_CERT_PATH");
            String certPassword = System.getenv("NFE_CERT_PASSWORD");
            
            if (certPath == null || certPath.isEmpty() || certPassword == null || certPassword.isEmpty()) {
                throw new IllegalStateException(
                    String.format("Certificado digital não configurado para ambiente '%s'. " +
                        "Configure as variáveis de ambiente NFE_CERT_PATH e NFE_CERT_PASSWORD", ambienteStr)
                );
            }
            
            certificado = CertificadoService.certificadoPfx(certPath, certPassword);
        } else {
            certificado = createMockCertificado();
        }
        
        return ConfiguracoesNfe.criarConfiguracoes(estado, ambiente, certificado, "src/main/resources/schemas");
    }
    
    private static Certificado createMockCertificado() {
        Certificado cert = new Certificado();
        cert.setNome("Certificado Mock - Desenvolvimento");
        cert.setVencimento(LocalDate.now().plusYears(1)); // Válido por 1 ano
        cert.setDiasRestantes(365L);
        cert.setCnpjCpf("12345678000195"); // CNPJ mock
        cert.setValido(true); // Marca como válido
        cert.setNumeroSerie(BigInteger.valueOf(123456789L));
        cert.setModoMultithreading(false);
        return cert;
    }
}
