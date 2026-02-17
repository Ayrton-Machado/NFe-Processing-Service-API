package com.erpservices.nfe.fiscal.xml.generator;

import com.erpservices.nfe.model.Invoice;
import br.com.swconsultoria.nfe.dom.ConfiguracoesNfe;
import br.com.swconsultoria.nfe.dom.enuns.DocumentoEnum;
import br.com.swconsultoria.nfe.schema_4.enviNFe.ObjectFactory;
import br.com.swconsultoria.nfe.schema_4.enviNFe.TNFe.InfNFe;
import br.com.swconsultoria.nfe.schema_4.enviNFe.TNFe.InfNFe.Total;
import br.com.swconsultoria.nfe.schema_4.enviNFe.TNFe.InfNFe.Transp;
import br.com.swconsultoria.nfe.schema_4.enviNFe.TNFe.InfNFe.Det.Imposto;
import br.com.swconsultoria.nfe.schema_4.enviNFe.TNFe.InfNFe.Det.Prod;
import br.com.swconsultoria.nfe.schema_4.enviNFe.TNFe.InfNFe.Det.Imposto.COFINS;
import br.com.swconsultoria.nfe.schema_4.enviNFe.TNFe.InfNFe.Det.Imposto.PIS;
import br.com.swconsultoria.nfe.schema_4.enviNFe.TNFe.InfNFe.Det.Imposto.COFINS.COFINSAliq;
import br.com.swconsultoria.nfe.schema_4.enviNFe.TNFe.InfNFe.Det.Imposto.PIS.PISAliq;
import br.com.swconsultoria.nfe.schema_4.enviNFe.TNFe.InfNFe.Total.ICMSTot;
import br.com.swconsultoria.nfe.schema_4.enviNFe.TEnderEmi;
import br.com.swconsultoria.nfe.schema_4.enviNFe.TEndereco;
import br.com.swconsultoria.nfe.schema_4.enviNFe.TLocal;
import br.com.swconsultoria.nfe.schema_4.enviNFe.TNFe;
import br.com.swconsultoria.nfe.schema_4.enviNFe.TUf;
import br.com.swconsultoria.nfe.schema_4.enviNFe.TUfEmi;
import br.com.swconsultoria.nfe.util.ChaveUtil;
import br.com.swconsultoria.nfe.util.ConstantesUtil;
import br.com.swconsultoria.nfe.util.XmlNfeUtil;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Gerador de XML da NF-e versão 4.00 conforme layout oficial.
 * 
 * Estrutura XML:
 * - <NFe>
 *   - <infNFe> (informações da nota)
 *     - <ide>    (identificação)
 *     - <emit>   (emitente)
 *     - <dest>   (destinatário)
 *     - <det>    (detalhamento de produtos/serviços)
 *     - <total>  (totalizadores)
 *     - <pag>    (pagamento)
 */
@ApplicationScoped
public class XmlGenerator {
    
    // Dados do Emitente
    @ConfigProperty(name = "nfe.emitente.cnpj", defaultValue = "00000000000191")
    String emitenteCnpj;
    
    @ConfigProperty(name = "nfe.emitente.razao-social", defaultValue = "EMPRESA TESTE LTDA")
    String emitenteRazaoSocial;
    
    @ConfigProperty(name = "nfe.emitente.ie", defaultValue = "1234567890")
    String emitenteIE;
    
    // Configurações de Impostos - ICMS
    @ConfigProperty(name = "nfe.icms.cst", defaultValue = "00")
    String icmsCst;
    
    @ConfigProperty(name = "nfe.icms.origem", defaultValue = "0")
    String icmsOrigem;
    
    @ConfigProperty(name = "nfe.icms.aliquota", defaultValue = "18.00")
    String icmsAliquota;
    
    // Configurações de Impostos - PIS
    @ConfigProperty(name = "nfe.pis.cst", defaultValue = "01")
    String pisCst;
    
    @ConfigProperty(name = "nfe.pis.aliquota", defaultValue = "1.65")
    String pisAliquota;
    
    // Configurações de Impostos - COFINS
    @ConfigProperty(name = "nfe.cofins.cst", defaultValue = "01")
    String cofinsCst;
    
    @ConfigProperty(name = "nfe.cofins.aliquota", defaultValue = "7.60")
    String cofinsAliquota;
    
    // Configurações de Impostos - IPI
    @ConfigProperty(name = "nfe.ipi.cenq", defaultValue = "999")
    String ipiCenq;
    
    @ConfigProperty(name = "nfe.ipi.cst", defaultValue = "53")
    String ipiCst;
    
    /**
     * Gera o XML completo da NF-e a partir de uma Invoice.
     * 
     * @param invoice a invoice contendo os dados da nota fiscal
     * @return TNFe objeto da nota fiscal eletrônica
     */
    public TNFe generate(Invoice invoice, ConfiguracoesNfe config) throws Exception {
        int numeroNfe = invoice.id.intValue();
        String cnpj = emitenteCnpj;
        LocalDateTime dataEmissao = invoice.issueDate;
        String cnf = String.format("%08d", new Random().nextInt(99999999));
        String modelo = DocumentoEnum.NFE.getModelo();
        int serie = 1;
        String tipoEmissao = "1";

        // MontaChave a NFe
        ChaveUtil chaveUtil = new ChaveUtil(config.getEstado(), cnpj, modelo, serie, numeroNfe, tipoEmissao, cnf, dataEmissao);
        String chave = chaveUtil.getChaveNF();
        String cdv = chaveUtil.getDigitoVerificador();

        // Monta Informe da NFe
        InfNFe infNFe = new InfNFe();
        infNFe.setId(chave);
        infNFe.setVersao(ConstantesUtil.VERSAO.NFE);

        // Preenche IDE
        infNFe.setIde(preencheIde(config, cnf, numeroNfe, tipoEmissao, modelo, serie, cdv, dataEmissao));

        // Preenche Emitente
        infNFe.setEmit(preencheEmitente(config, cnpj));

        // Preenche o Destinatario
        infNFe.setDest(preencheDestinatario());
        infNFe.setEntrega(dadosEntrega(infNFe));

        // Preenche os dados do Produto da Nfe e adiciona a Lista
        infNFe.getDet().addAll(preencheDet());

        // Preenche totais da NFe
        infNFe.setTotal(preencheTotal());

        // Preenche os dados de Transporte
        infNFe.setTransp(preencheTransporte());

        // Preenche dados Pagamento
        infNFe.setPag(preenchePag());

        // Preenche informações adicionais
        infNFe.setInfAdic(montaInfAdic());

        // Preenche as Informações de Intermediador
        infNFe.setInfIntermed(montaInfInterm());

        // Monta objeto da NFe
        TNFe nfe = new TNFe();
        nfe.setInfNFe(infNFe);
        
        // Adiciona informações suplementares
        nfe.setInfNFeSupl(montaInfNFeSupl(chave));

        // Imprime XML gerado (apenas para debug)
        System.out.println(XmlNfeUtil.objectToXml(nfe));

        return nfe;
    }
    

    private static InfNFe.InfIntermed montaInfInterm() {
        InfNFe.InfIntermed infIntermed = new InfNFe.InfIntermed();
        infIntermed.setCNPJ("46971895000102");
        infIntermed.setIdCadIntTran("JOao Intermediarios SA");
        return infIntermed;
    }

    private static InfNFe.InfAdic montaInfAdic() {
        InfNFe.InfAdic infAdic = new InfNFe.InfAdic();
        infAdic.setInfCpl("Observacao teste");

        return infAdic;
    }
    
    /**
     * Monta as informações suplementares da NF-e (infNFeSupl).
     * Gera QR Code em formato válido conforme padrão V2 ONLINE.
     * 
     * Formato QR Code V2 ONLINE:
     * https://URL?p=CHAVE|2|AMBIENTE|VALOR|HASH
     * 
     * @param chaveNfe a chave de acesso da NF-e (pode vir com prefixo "NFe")
     * @return objeto TNFe.InfNFeSupl preenchido
     */
    private static TNFe.InfNFeSupl montaInfNFeSupl(String chaveNfe) {
        TNFe.InfNFeSupl infNFeSupl = new TNFe.InfNFeSupl();
        
        // Remove o prefixo "NFe" se existir (QR Code exige apenas os 44 dígitos)
        String chave44Digitos = chaveNfe.startsWith("NFe") ? chaveNfe.substring(3) : chaveNfe;
        
        // QR Code V2 ONLINE - formato validado pelo XSD
        // Padrão: ((HTTPS?|https?)://.*\?p=([0-9]{34}(1|3|4)[0-9]{9})\|[2]\|[1-2]\|(0|[1-9]{1}([0-9]{1,5})?)\|[A-Fa-f0-9]{40})
        String qrCode = String.format(
            "https://www.fazenda.sp.gov.br/nfce/qrcode?p=%s|2|1|0|%s",
            chave44Digitos,
            "0000000000000000000000000000000000000000" // Hash placeholder (40 caracteres hex)
        );
        
        infNFeSupl.setQrCode(qrCode);
        infNFeSupl.setUrlChave("https://www.fazenda.sp.gov.br/nfce/consulta");
        
        return infNFeSupl;
    }
    
    private static InfNFe.Ide preencheIde(ConfiguracoesNfe config, String cnf, int numeroNfe, String tipoEmissao, String modelo, int serie, String cDv, LocalDateTime dataEmissao) {
        InfNFe.Ide ide = new InfNFe.Ide();
        ide.setCUF(config.getEstado().getCodigoUF());
        ide.setCNF(cnf);
        ide.setNatOp("NOTA FISCAL CONSUMIDOR ELETRONICA");
        ide.setMod(modelo);
        ide.setSerie(String.valueOf(serie));

        ide.setNNF(String.valueOf(numeroNfe));
        ide.setDhEmi(XmlNfeUtil.dataNfe(dataEmissao, null));
        ide.setTpNF("1");
        ide.setIdDest("1");
        ide.setCMunFG("5219753");
        ide.setTpImp("1");
        ide.setTpEmis(tipoEmissao);
        ide.setCDV(cDv);
        ide.setTpAmb(config.getAmbiente().getCodigo());
        ide.setFinNFe("1");
        ide.setIndFinal("1");
        ide.setIndPres("2");
        ide.setProcEmi("0");
        ide.setVerProc("1.0");
        ide.setIndIntermed("1");

        return ide;
    }

    
        private static InfNFe.Emit preencheEmitente(ConfiguracoesNfe config, String cnpj) {
        InfNFe.Emit emit = new InfNFe.Emit();
        emit.setCNPJ(cnpj);
        emit.setXNome("POSTO PARK XIII LTDA");

        TEnderEmi enderEmit = new TEnderEmi();
        enderEmit.setXLgr("AV SANTO ANTONIO cia");
        enderEmit.setNro("0");
        enderEmit.setXCpl("QD 17 LT 01-02-03");
        enderEmit.setXBairro("PQ STO ANTONIO");
        enderEmit.setCMun("5219753");
        enderEmit.setXMun("SANTO ANTONIO DO DESCOBERTO");
        enderEmit.setUF(TUfEmi.valueOf(config.getEstado().toString()));
        enderEmit.setCEP("72900000");
        enderEmit.setCPais("1058");
        enderEmit.setXPais("Brasil");
        enderEmit.setFone("6233215175");
        emit.setEnderEmit(enderEmit);

        emit.setIE("104519304");
        emit.setCRT("3");

        return emit;
    }

    /**
     * Preenche o Destinatario da NFe
     * @return
     */
    private static InfNFe.Dest preencheDestinatario() {
        InfNFe.Dest dest = new InfNFe.Dest();
        dest.setCPF("12345678901");
        dest.setXNome("NF-E EMITIDA EM AMBIENTE DE HOMOLOGACAO - SEM VALOR FISCAL");

        TEndereco enderDest = new TEndereco();
        enderDest.setXLgr("Rua: Teste");
        enderDest.setNro("0");
        enderDest.setXBairro("TESTE");
        enderDest.setCMun("5202809");
        enderDest.setXMun("AVELINOPOLIS");
        enderDest.setUF(TUf.valueOf("GO"));
        enderDest.setCEP("74430130");
        enderDest.setCPais("1058");
        enderDest.setXPais("Brasil");
        enderDest.setFone("4845454545");
        dest.setEnderDest(enderDest);
        dest.setEmail("teste@test");
        dest.setIE("109684036");
        dest.setIndIEDest("1");
        return dest;
    }

    /**
     * Preenche Det Nfe
     */
    private static List<InfNFe.Det> preencheDet() {

        //O Preenchimento deve ser feito por produto, Então deve ocorrer uma LIsta
        InfNFe.Det det = new InfNFe.Det();
        //O numero do Item deve seguir uma sequencia
        det.setNItem("1");

        // Preenche dados do Produto
        det.setProd(preencheProduto());

        //Preenche dados do Imposto
        det.setImposto(preencheImposto());

        det.setInfAdProd("Informações Adicionais do Produto");

        //Retorna a Lista de Det
        return Collections.singletonList(det);
    }

    /**
     * Preenche dados do Produto
     * @return
     */
    private static Prod preencheProduto() {
        Prod prod = new Prod();
        prod.setCProd("7898480650104");
        prod.setCEAN("7898480650104");
        prod.setXProd("NOTA FISCAL EMITIDA EM AMBIENTE DE HOMOLOGACAO - SEM VALOR FISCAL");
        prod.setNCM("27101932");
        prod.setCEST("0600500");
        prod.setIndEscala("S");
        prod.setCFOP("5405");
        prod.setUCom("UN");
        prod.setQCom("1.0000");
        prod.setVUnCom("13.0000");
        prod.setVProd("13.00");
        prod.setCEANTrib("7898480650104");
        prod.setUTrib("UN");
        prod.setQTrib("1.0000");
        prod.setVUnTrib("13.0000");
        prod.setIndTot("1");

        return prod;
    }

    /**
     * Preenche dados do Imposto da Nfe
     * @return
     */
    private static Imposto preencheImposto() {
        Imposto imposto = new Imposto();

        Imposto.ICMS icms = new Imposto.ICMS();

        Imposto.ICMS.ICMS60 icms60 = new Imposto.ICMS.ICMS60();
        icms60.setOrig("0");
        icms60.setCST("60");
        icms60.setVBCSTRet("0.00");
        icms60.setPST("0.00");
        icms60.setVICMSSTRet("0.00");
        icms60.setVICMSSubstituto("0.00");

        icms.setICMS60(icms60);

        PIS pis = new PIS();
        PISAliq pisAliq = new PISAliq();
        pisAliq.setCST("01");
        pisAliq.setVBC("13.00");
        pisAliq.setPPIS("1.65");
        pisAliq.setVPIS("0.21");
        pis.setPISAliq(pisAliq);

        COFINS cofins = new COFINS();
        COFINSAliq cofinsAliq = new COFINSAliq();
        cofinsAliq.setCST("01");
        cofinsAliq.setVBC("13.00");
        cofinsAliq.setPCOFINS("7.60");
        cofinsAliq.setVCOFINS("0.99");
        cofins.setCOFINSAliq(cofinsAliq);

        imposto.getContent().add(new ObjectFactory().createTNFeInfNFeDetImpostoVTotTrib("5.00"));
        imposto.getContent().add(new ObjectFactory().createTNFeInfNFeDetImpostoICMS(icms));
        imposto.getContent().add(new ObjectFactory().createTNFeInfNFeDetImpostoPIS(pis));
        imposto.getContent().add(new ObjectFactory().createTNFeInfNFeDetImpostoCOFINS(cofins));

        return imposto;
    }

    /**
     * Prenche Total NFe
     * @return
     */
    private static Total preencheTotal() {
        Total total = new Total();
        ICMSTot icmstot = new ICMSTot();
        icmstot.setVBC("0.00");
        icmstot.setVICMS("0.00");
        icmstot.setVICMSDeson("0.00");
        icmstot.setVFCP("0.00");
        icmstot.setVFCPST("0.00");
        icmstot.setVFCPSTRet("0.00");
        icmstot.setVBCST("0.00");
        icmstot.setVST("0.00");
        icmstot.setVProd("13.00");
        icmstot.setVFrete("0.00");
        icmstot.setVSeg("0.00");
        icmstot.setVDesc("0.00");
        icmstot.setVII("0.00");
        icmstot.setVIPI("0.00");
        icmstot.setVIPIDevol("0.00");
        icmstot.setVPIS("0.21");
        icmstot.setVCOFINS("0.99");
        icmstot.setVOutro("0.00");
        icmstot.setVNF("13.00");
        icmstot.setVTotTrib("5.00");
        total.setICMSTot(icmstot);

        return total;
    }

    /**
     * Preenche Transporte
     * @return
     */
    private static Transp preencheTransporte() {
        Transp transp = new Transp();
        transp.setModFrete("9");
        return transp;
    }

    /**
     * Preenche dados Pagamento
     * @return
     */
    private static InfNFe.Pag preenchePag() {
        InfNFe.Pag pag = new InfNFe.Pag();
        InfNFe.Pag.DetPag detPag = new InfNFe.Pag.DetPag();
        detPag.setTPag("01");
        detPag.setVPag("13.00");
        pag.getDetPag().add(detPag);

        return pag;
    }

    // Criado para evitar UFIdDest
    private static TLocal dadosEntrega(InfNFe infNFe) {
        TLocal entrega = new TLocal();

        entrega.setCPF(infNFe.getDest().getCPF());

        entrega.setXLgr(infNFe.getEmit().getEnderEmit().getXLgr());
        entrega.setNro(infNFe.getEmit().getEnderEmit().getNro());
        entrega.setXBairro(infNFe.getEmit().getEnderEmit().getXBairro());
        entrega.setCMun(infNFe.getEmit().getEnderEmit().getCMun());
        entrega.setXMun(infNFe.getEmit().getEnderEmit().getXMun());
        entrega.setUF(TUf.valueOf(infNFe.getEmit().getEnderEmit().getUF().toString()));
        return entrega;
    }

}
