package com.erpservices.nfe.fiscal.impressao;

import br.com.swconsultoria.nfe.impressao.ImpressaoDTO;
import br.com.swconsultoria.nfe.impressao.ImpressaoNfeUtil;
import br.com.swconsultoria.nfe.impressao.JasperNFeEnum;
import jakarta.enterprise.context.ApplicationScoped;
import static com.erpservices.nfe.fiscal.impressao.ConstantesImpressaoNfeMock.*;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import static br.com.swconsultoria.nfe.impressao.ConstantesImpressaoNfeUtil.*;


@ApplicationScoped
public class DanfeService {
    @ConfigProperty(name = "nfe.ambiente", defaultValue = "test")
    String ambiente;
    /**
     * Gera o DANFE (PDF) a partir do XML.
     * 
     * @param xml XML completo com protocolo de autorização (para testes aponta para xml nao assinada com MOCKED_PATH_NFE)
     * @param nomeArquivo Nome do arquivo PDF a ser gerado (ex: "danfe123456.pdf")
     * @return Caminho completo do arquivo gerado, ou null em caso de erro
     */

    public String gerarDanfe(String xml, String nomeArquivo) {
        try {
            // Define caminho completo do arquivo
            String caminhoCompleto = "/home/k8s/Documents/medo/projetos/NFe-Processing-Service-API/src/main/resources/danfes/" + nomeArquivo + ".pdf";

            // Gera layout padrão
            ImpressaoDTO impressao = new ImpressaoDTO();
            impressao.setXml(xml);

            // Define nfe utilizada para geração do DANFE
            if (ambiente.equals("prod") || ambiente.equals("homolog")) {
                // NÃO-TESTADO
                impressao.setPathExpression(PATH_NFE);
            } else {
                System.out.println("\nDANFE: XML sem assinatura (elementos e dados de assinatura não presentes) utilizado");
                impressao.setPathExpression(MOCKED_PATH_NFE);
            }

            impressao.setJasper(JasperNFeEnum.NFE.getJasper());
            impressao.getParametros().put(PARAM_LOGO_NFE, ImpressaoNfeUtil.class.getResourceAsStream(PATH_LOGO_NFE));
            impressao.getParametros().put("SUBREPORT", JasperNFeEnum.NFE_FATURA.getJasper());

            // Exporta para PDF
            ImpressaoNfeUtil.impressaoPdfArquivo(impressao, caminhoCompleto);
            
            System.out.println("DANFE gerado com sucesso, caminho: " + caminhoCompleto);

            return caminhoCompleto;
            
        } catch (Exception e) {
            System.err.println("Erro ao gerar DANFE: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}
