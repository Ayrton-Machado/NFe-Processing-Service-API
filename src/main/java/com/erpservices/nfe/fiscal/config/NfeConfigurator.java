package com.erpservices.nfe.fiscal.config;

import br.com.swconsultoria.certificado.Certificado;
import br.com.swconsultoria.certificado.CertificadoService;
import br.com.swconsultoria.certificado.TipoCertificadoEnum;
import br.com.swconsultoria.nfe.dom.ConfiguracoesNfe;
import br.com.swconsultoria.nfe.dom.enuns.AmbienteEnum;
import br.com.swconsultoria.nfe.dom.enuns.EstadosEnum;
import jakarta.enterprise.context.ApplicationScoped;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.math.BigInteger;
import java.time.LocalDate;

@ApplicationScoped
public class NfeConfigurator {

    @ConfigProperty(name = "nfe.ambiente", defaultValue = "test")
    String ambiente;

    @ConfigProperty(name = "nfe.emitente.cnpj", defaultValue = "00000000000191")
    String emitenteCnpj;

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
    /**
     * Inicializa configurações da NFe.
     * Em ambiente test: usa certificado mock.
     * Em homolog/prod: usa certificado configurado via variáveis de ambiente.
     */
    public ConfiguracoesNfe initConfigNfe(EstadosEnum estado, String ambienteStr) throws Exception {
        AmbienteEnum ambiente = getAmbiente(ambienteStr);
        Certificado certificado;
        
        if (ambienteStr.equals("prod") || ambienteStr.equals("homolog")) {
            String certificadoCaminho = System.getenv("NFE_CERT_PATH");
            String certificadoSenha = System.getenv("NFE_CERT_PASSWORD");
            
            if (certificadoCaminho == null || certificadoCaminho.isEmpty() || certificadoSenha == null || certificadoSenha.isEmpty()) {
                throw new IllegalStateException(
                    String.format("Certificado digital não configurado para ambiente '%s'. " +
                        "Configure as variáveis de ambiente NFE_CERT_PATH e NFE_CERT_PASSWORD", ambienteStr)
                );
            }
            
            certificado = CertificadoService.certificadoPfx(certificadoCaminho, certificadoSenha);
        } else {
            certificado = createMockCertificado(emitenteCnpj);
        }
        
        return ConfiguracoesNfe.criarConfiguracoes(estado, ambiente, certificado, "src/main/resources/schemas");
    }
    
    private static Certificado createMockCertificado(String emitenteCnpj) {
        Certificado cert = new Certificado();
        cert.setNome("Certificado Mock - Desenvolvimento");
        cert.setVencimento(LocalDate.now().plusYears(1)); // Válido por 1 ano
        cert.setDiasRestantes(365L);
        cert.setCnpjCpf(emitenteCnpj); // CNPJ mock
        cert.setValido(true); // Marca como válido
        cert.setNumeroSerie(BigInteger.valueOf(123456789L));
        cert.setModoMultithreading(false);
        cert.setTipoCertificado(TipoCertificadoEnum.ARQUIVO);
        return cert;
    }
}
