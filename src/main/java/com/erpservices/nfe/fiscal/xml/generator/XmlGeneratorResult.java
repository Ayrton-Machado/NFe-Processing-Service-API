package com.erpservices.nfe.fiscal.xml.generator;
import br.com.swconsultoria.nfe.schema_4.enviNFe.TNFe;

public class XmlGeneratorResult {
    public TNFe tNFe;
    public String chaveAcessoNfe;

    public XmlGeneratorResult(TNFe tNFe, String chaveAcessoNfe) {
        this.tNFe = tNFe;
        this.chaveAcessoNfe = chaveAcessoNfe;
    }
}
