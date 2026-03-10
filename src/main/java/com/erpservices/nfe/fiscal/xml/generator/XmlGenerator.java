package com.erpservices.nfe.fiscal.xml.generator;

import com.erpservices.nfe.model.Invoice;
import com.erpservices.nfe.model.InvoiceItem;

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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
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

    // Adicionar após emitenteIE:
    @ConfigProperty(name = "nfe.emitente.crt", defaultValue = "3")
    String emitenteCrt;

    @ConfigProperty(name = "nfe.emitente.logradouro", defaultValue = "RUA TESTE")
    String emitenteLogradouro;

    @ConfigProperty(name = "nfe.emitente.numero", defaultValue = "0")
    String emitenteNumero;

    @ConfigProperty(name = "nfe.emitente.complemento", defaultValue = "")
    String emitenteComplemento;

    @ConfigProperty(name = "nfe.emitente.bairro", defaultValue = "BAIRRO TESTE")
    String emitenteBairro;

    @ConfigProperty(name = "nfe.emitente.codigo-municipio", defaultValue = "0000000")
    String emitenteCodigoMunicipio;

    @ConfigProperty(name = "nfe.emitente.municipio", defaultValue = "MUNICIPIO TESTE")
    String emitenteMunicipio;

    @ConfigProperty(name = "nfe.emitente.cep", defaultValue = "00000000")
    String emitenteCep;

    @ConfigProperty(name = "nfe.emitente.fone", defaultValue = "")
    String emitenteFone;
    
    /**
     * Gera o XML completo da NF-e a partir de uma Invoice.
     * 
     * @param invoice a invoice contendo os dados da nota fiscal
     * @return TNFe objeto da nota fiscal eletrônica
     */
    public XmlGeneratorResult generate(Invoice invoice, ConfiguracoesNfe config) throws Exception {
        int numeroNfe = invoice.number.intValue();
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
        infNFe.setIde(preencheIde(config, cnf, numeroNfe, tipoEmissao, modelo, serie, cdv, dataEmissao, emitenteCodigoMunicipio));
        // Preenche Emitente
        infNFe.setEmit(preencheEmitente(config, cnpj));
        
        // Preenche o Destinatario
        infNFe.setDest(preencheDestinatario(invoice));
        infNFe.setEntrega(dadosEntrega(infNFe));
        
        // Preenche os dados do Produto da Nfe e adiciona a Lista
        infNFe.getDet().addAll(preencheDet(invoice.items));

        // Preenche totais da NFe
        infNFe.setTotal(preencheTotal(invoice.totalAmount, invoice.items));

        // Preenche os dados de Transporte
        infNFe.setTransp(preencheTransporte());

        // Preenche dados Pagamento
        infNFe.setPag(preenchePag(invoice.totalAmount));

        // Preenche informações adicionais
        infNFe.setInfAdic(montaInfAdic());

        // Monta objeto da NFe
        TNFe tnfe = new TNFe();
        tnfe.setInfNFe(infNFe);
        
        // Adiciona informações suplementares
        tnfe.setInfNFeSupl(montaInfNFeSupl(chave));

        // Imprime XML gerado (apenas para debug)
        System.out.println(XmlNfeUtil.objectToXml(tnfe));

        String chaveAcesso44 = chave.startsWith("NFe") ? chave.substring(3) : chave;

        return new XmlGeneratorResult(tnfe, chaveAcesso44);
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
    
    private static InfNFe.Ide preencheIde(ConfiguracoesNfe config, String cnf, int numeroNfe, String tipoEmissao, String modelo, int serie, String cDv, LocalDateTime dataEmissao, String codigoMunicipioEmitente) {
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
        ide.setCMunFG(codigoMunicipioEmitente);
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

    
    private InfNFe.Emit preencheEmitente(ConfiguracoesNfe config, String cnpj) {
        InfNFe.Emit emit = new InfNFe.Emit();
        emit.setCNPJ(cnpj);
        emit.setXNome(emitenteRazaoSocial);

        TEnderEmi enderEmit = new TEnderEmi();
        enderEmit.setXLgr(emitenteLogradouro);
        enderEmit.setNro(emitenteNumero);
        enderEmit.setXCpl(emitenteComplemento);
        enderEmit.setXBairro(emitenteBairro);
        enderEmit.setCMun(emitenteCodigoMunicipio);
        enderEmit.setXMun(emitenteMunicipio);
        enderEmit.setUF(TUfEmi.valueOf(config.getEstado().toString()));
        enderEmit.setCEP(emitenteCep);
        enderEmit.setCPais("1058");
        enderEmit.setXPais("Brasil");
        enderEmit.setFone(emitenteFone);
        emit.setEnderEmit(enderEmit);

        emit.setIE(emitenteIE);                      // ✅ vem do application.properties
        emit.setCRT(emitenteCrt);       

        return emit;
    }

    /**
     * Preenche o Destinatario da NFe
     * @return
     */
    private static InfNFe.Dest preencheDestinatario(Invoice invoice) {
        InfNFe.Dest dest = new InfNFe.Dest();
        dest.setCPF(invoice.customerCpf);
        dest.setXNome("NF-E EMITIDA EM AMBIENTE DE HOMOLOGACAO - SEM VALOR FISCAL");
        
        
        TEndereco enderDest = new TEndereco();
        enderDest.setXLgr(invoice.customerStreet);
        enderDest.setNro(invoice.customerNumber);
        enderDest.setXBairro(invoice.customerDistrict);
        enderDest.setCMun(invoice.customerCityCode);
        enderDest.setXMun(invoice.customerCity);
        enderDest.setUF(TUf.valueOf(invoice.customerState.trim().toUpperCase()));
        enderDest.setCEP(invoice.customerZipCode);
        enderDest.setCPais("1058");
        enderDest.setXPais("Brasil");
        enderDest.setFone(null);
        dest.setEnderDest(enderDest);
        dest.setEmail(invoice.customerEmail);
        dest.setIE(null);
        dest.setIndIEDest("9");
        return dest;
    }

    /**
     * Preenche Det Nfe
     */
    private List<InfNFe.Det> preencheDet(List<InvoiceItem> items) {

        //O Preenchimento deve ser feito por produto, Então deve ocorrer uma LIsta
        List<InfNFe.Det> dets = new ArrayList<>();

        for (int i = 0; i < items.size(); i++) {
            InvoiceItem item = items.get(i); // pega o item atual

            //O numero do Item deve seguir uma sequencia
            InfNFe.Det det = new InfNFe.Det();
            det.setNItem(String.valueOf(i+1));

            //Preenche dados do Produto
            det.setProd(preencheProduto(item));
            //Preenche dados do Imposto
            det.setImposto(preencheImposto(item));
            //Preenche dados adicionais
            det.setInfAdProd("Informações Adicionais do Produto");

            dets.add(det);
        };
        
        //Retorna a Lista de Det
        return dets;
    }

    /**
     * Preenche dados do Produto
     * @return
     */
    private static Prod preencheProduto(InvoiceItem item) {
        Prod prod = new Prod();
        prod.setCProd(item.productCode);
        prod.setCEAN("SEM GTIN");
        prod.setXProd("NOTA FISCAL EMITIDA EM AMBIENTE DE HOMOLOGACAO - SEM VALOR FISCAL");
        prod.setNCM("27101932");
        prod.setCEST("0600500");
        prod.setIndEscala("S");
        prod.setCFOP("5102"); // Venda de mercadoria adquirida de terceiros - tributação normal (CST 00)
        prod.setUCom("UN");
        prod.setQCom(String.format(Locale.US, "%.4f", (double) item.quantity));
        prod.setVUnCom(String.format(Locale.US, "%.4f", item.unitPrice));
        prod.setVProd(String.format(Locale.US, "%.2f", item.totalPrice));
        prod.setCEANTrib("SEM GTIN");
        prod.setUTrib("UN");
        prod.setQTrib(String.format(Locale.US, "%.4f", (double) item.quantity));
        prod.setVUnTrib(String.format(Locale.US, "%.4f", item.unitPrice));
        prod.setIndTot("1");

        return prod;
    }

    /**
     * Preenche dados do Imposto da Nfe calculando ICMS, PIS e COFINS com base no totalPrice do item.
     * Utiliza ICMS00 (tributação normal) com alíquota configurada em nfe.icms.aliquota.
     * @param item item da nota fiscal
     * @return
     */
    private Imposto preencheImposto(InvoiceItem item) {
        Imposto imposto = new Imposto();

        Imposto.ICMS icms = new Imposto.ICMS();

        BigDecimal vBC = item.totalPrice;

        BigDecimal aliquotaICMS = new BigDecimal(icmsAliquota);
        BigDecimal vICMS = vBC.multiply(aliquotaICMS)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        Imposto.ICMS.ICMS00 icms00 = new Imposto.ICMS.ICMS00();
        icms00.setOrig(icmsOrigem);
        icms00.setCST("00");
        icms00.setModBC("3");
        icms00.setVBC(String.format(Locale.US, "%.2f", vBC));
        icms00.setPICMS(icmsAliquota);
        icms00.setVICMS(String.format(Locale.US, "%.2f", vICMS));
        icms.setICMS00(icms00);

        BigDecimal vPIS = vBC.multiply(new BigDecimal(pisAliquota))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        BigDecimal vCOFINS = vBC.multiply(new BigDecimal(cofinsAliquota))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        BigDecimal vTotTrib = vICMS.add(vPIS).add(vCOFINS);

        PIS pis = new PIS();
        PISAliq pisAliq = new PISAliq();
        pisAliq.setCST(pisCst);
        pisAliq.setVBC(String.format(Locale.US, "%.2f", vBC));
        pisAliq.setPPIS(pisAliquota);
        pisAliq.setVPIS(String.format(Locale.US, "%.2f", vPIS));
        pis.setPISAliq(pisAliq);

        COFINS cofins = new COFINS();
        COFINSAliq cofinsAliq = new COFINSAliq();
        cofinsAliq.setCST(cofinsCst);
        cofinsAliq.setVBC(String.format(Locale.US, "%.2f", vBC));
        cofinsAliq.setPCOFINS(cofinsAliquota);
        cofinsAliq.setVCOFINS(String.format(Locale.US, "%.2f", vCOFINS));
        cofins.setCOFINSAliq(cofinsAliq);

        imposto.getContent().add(new ObjectFactory().createTNFeInfNFeDetImpostoVTotTrib(String.format(Locale.US, "%.2f", vTotTrib)));
        imposto.getContent().add(new ObjectFactory().createTNFeInfNFeDetImpostoICMS(icms));
        imposto.getContent().add(new ObjectFactory().createTNFeInfNFeDetImpostoPIS(pis));
        imposto.getContent().add(new ObjectFactory().createTNFeInfNFeDetImpostoCOFINS(cofins));

        return imposto;
    }

    /**
     * Preenche Total NFe somando ICMS, PIS e COFINS calculados por item.
     * @param totalAmount valor total da nota fiscal
     * @param items lista de itens da nota fiscal
     * @return
     */
    private Total preencheTotal(BigDecimal totalAmount, List<InvoiceItem> items) {
        String vTotal = String.format(Locale.US, "%.2f", totalAmount);

        BigDecimal totalBC = BigDecimal.ZERO;
        BigDecimal totalICMS = BigDecimal.ZERO;
        BigDecimal totalPIS = BigDecimal.ZERO;
        BigDecimal totalCOFINS = BigDecimal.ZERO;

        BigDecimal aliquotaICMS = new BigDecimal(icmsAliquota);

        for (InvoiceItem item : items) {
            BigDecimal vBC = item.totalPrice;
            totalBC = totalBC.add(vBC);
            totalICMS = totalICMS.add(
                vBC.multiply(aliquotaICMS)
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP));
            totalPIS = totalPIS.add(
                vBC.multiply(new BigDecimal(pisAliquota))
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP));
            totalCOFINS = totalCOFINS.add(
                vBC.multiply(new BigDecimal(cofinsAliquota))
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP));
        }

        BigDecimal vTotTrib = totalICMS.add(totalPIS).add(totalCOFINS);

        Total total = new Total();
        ICMSTot icmstot = new ICMSTot();
        icmstot.setVBC(String.format(Locale.US, "%.2f", totalBC));
        icmstot.setVICMS(String.format(Locale.US, "%.2f", totalICMS));
        icmstot.setVICMSDeson("0.00");
        icmstot.setVFCP("0.00");
        icmstot.setVFCPST("0.00");
        icmstot.setVFCPSTRet("0.00");
        icmstot.setVBCST("0.00");
        icmstot.setVST("0.00");
        icmstot.setVProd(vTotal);
        icmstot.setVFrete("0.00");
        icmstot.setVSeg("0.00");
        icmstot.setVDesc("0.00");
        icmstot.setVII("0.00");
        icmstot.setVIPI("0.00");
        icmstot.setVIPIDevol("0.00");
        icmstot.setVPIS(String.format(Locale.US, "%.2f", totalPIS));
        icmstot.setVCOFINS(String.format(Locale.US, "%.2f", totalCOFINS));
        icmstot.setVOutro("0.00");
        icmstot.setVNF(vTotal);
        icmstot.setVTotTrib(String.format(Locale.US, "%.2f", vTotTrib));
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
     * @param totalAmount valor total da nota fiscal
     * @return
     */
    private static InfNFe.Pag preenchePag(BigDecimal totalAmount) {
        InfNFe.Pag pag = new InfNFe.Pag();
        InfNFe.Pag.DetPag detPag = new InfNFe.Pag.DetPag();
        detPag.setTPag("01");
        detPag.setVPag(String.format(Locale.US, "%.2f", totalAmount));
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
