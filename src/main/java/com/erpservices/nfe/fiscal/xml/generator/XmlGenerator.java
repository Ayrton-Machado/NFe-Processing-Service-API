package com.erpservices.nfe.fiscal.xml.generator;

import com.erpservices.nfe.model.Invoice;
import com.erpservices.nfe.model.InvoiceItem;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

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
    
    private static final String NAMESPACE = "http://www.portalfiscal.inf.br/nfe";
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX");
    
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
     * @return String contendo o XML da NF-e (sem assinatura digital)
     */
    public String generate(Invoice invoice) {
        StringBuilder xml = new StringBuilder();
        
        // Declaração XML
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        
        // Tag raiz <NFe>
        xml.append("<NFe xmlns=\"").append(NAMESPACE).append("\">");
        
        // Tag <infNFe> (informações da nota)
        xml.append("<infNFe Id=\"NFe").append(generateChaveAcesso(invoice)).append("\" versao=\"4.00\">");
        
        // Seções principais
        appendIde(xml, invoice);      // Identificação
        appendEmit(xml);              // Emitente
        appendDest(xml, invoice);     // Destinatário
        appendDet(xml, invoice);      // Detalhamento (produtos)
        appendTotal(xml, invoice);    // Totalizadores
        appendPag(xml, invoice);      // Pagamento
        appendTransp(xml);            // Transporte
        appendInfAdic(xml);           // Informações adicionais
        
        xml.append("</infNFe>");
        xml.append("</NFe>");
        
        return xml.toString();
    }
    
    /**
     * Gera chave de acesso (44 dígitos).
     * TODO: Implementar cálculo real da chave conforme especificação.
     */
    private String generateChaveAcesso(Invoice invoice) {
        // Por enquanto, chave fake para testes
        // Formato: UF(2) + AAMM(4) + CNPJ(14) + MOD(2) + SERIE(3) + NNF(9) + TPEMIS(1) + CNFE(8) + DV(1)
        return "41260100000000000001915500100000000010000000001";
    }
    
    /**
     * Seção <ide> - Identificação da NF-e.
     * Campos obrigatórios conforme XSD.
     */
    private void appendIde(StringBuilder xml, Invoice invoice) {
        xml.append("<ide>");
        
        // TODO: Adicionar campos obrigatórios do <ide>:
        xml.append("<cUF>41</cUF>");                    // Código UF (41 = Paraná)
        // TODO: Gerar número aleatório de 8 dígitos para segurança
        xml.append("<cNF>00000001</cNF>");              // Código numérico (deve ser aleatório)
        xml.append("<natOp>Venda de mercadoria</natOp>"); // Natureza da operação
        xml.append("<mod>55</mod>");                    // Modelo (55 = NF-e)
        xml.append("<serie>1</serie>");                 // Série
        xml.append("<nNF>").append(invoice.invoiceNumber).append("</nNF>"); // Número da nota
        xml.append("<dhEmi>").append(formatDateTime(invoice.issueDate)).append("</dhEmi>"); // Data/hora emissão
        xml.append("<tpNF>1</tpNF>");                   // Tipo (1 = Saída)
        xml.append("<idDest>1</idDest>");               // Destino (1 = Operação interna)
        xml.append("<cMunFG>4106902</cMunFG>");         // Município fiscal geradora (Curitiba)
        xml.append("<tpImp>1</tpImp>");                 // Formato impressão (1 = DANFE retrato)
        xml.append("<tpEmis>1</tpEmis>");               // Tipo emissão (1 = Normal)
        // cDV é calculado automaticamente como parte da chave de acesso
        xml.append("<tpAmb>2</tpAmb>");                 // Ambiente (2 = Homologação)
        xml.append("<finNFe>1</finNFe>");               // Finalidade (1 = Normal)
        xml.append("<indFinal>1</indFinal>");           // Consumidor final (1 = Sim)
        xml.append("<indPres>1</indPres>");             // Indicador presença (1 = Operação presencial)
        xml.append("<procEmi>0</procEmi>");             // Processo emissão (0 = Próprio)
        xml.append("<verProc>1.0.0</verProc>");         // Versão do processo de emissão

        xml.append("</ide>");
    }
    
    /**
     * Seção <emit> - Dados do Emitente.
     */
    private void appendEmit(StringBuilder xml) {
        xml.append("<emit>");
        
        // TODO: Adicionar campos obrigatórios do <emit>:
        xml.append("<CNPJ>").append(emitenteCnpj).append("</CNPJ>");
        xml.append("<xNome>").append(emitenteRazaoSocial).append("</xNome>");
        xml.append("<xFant>").append(emitenteRazaoSocial).append("</xFant>");
        
        // Endereço do emitente
        xml.append("<enderEmit>");
        xml.append("<xLgr>Rua Exemplo</xLgr>");         // TODO: Configurar endereço real
        xml.append("<nro>123</nro>");
        xml.append("<xBairro>Centro</xBairro>");
        xml.append("<cMun>4106902</cMun>");             // Código município (Curitiba)
        xml.append("<xMun>Curitiba</xMun>");
        xml.append("<UF>PR</UF>");
        xml.append("<CEP>80000000</CEP>");
        xml.append("</enderEmit>");
        
        xml.append("<IE>").append(emitenteIE).append("</IE>"); // Inscrição Estadual
        xml.append("<CRT>3</CRT>");                     // Regime tributário (3 = Regime Normal)
        
        xml.append("</emit>");
    }
    
    /**
     * Seção <dest> - Dados do Destinatário.
     */
    private void appendDest(StringBuilder xml, Invoice invoice) {
        xml.append("<dest>");
        
        // TODO: Adicionar campos obrigatórios do <dest>:
        xml.append("<CPF>").append(invoice.customerCpf).append("</CPF>");
        xml.append("<xNome>").append(escapeXml(invoice.customerName)).append("</xNome>");
        
        // Endereço do destinatário
        xml.append("<enderDest>");
        xml.append("<xLgr>Rua do Cliente</xLgr>");      // TODO: Pegar do banco
        xml.append("<nro>456</nro>");
        xml.append("<xBairro>Jardim</xBairro>");
        xml.append("<cMun>4106902</cMun>");
        xml.append("<xMun>Curitiba</xMun>");
        xml.append("<UF>PR</UF>");
        xml.append("<CEP>81000000</CEP>");
        xml.append("</enderDest>");
        
        xml.append("<indIEDest>9</indIEDest>");         // 9 = Não contribuinte
        xml.append("<email>").append(invoice.customerEmail).append("</email>");
        
        xml.append("</dest>");
    }
    
    /**
     * Seção <det> - Detalhamento de Produtos/Serviços.
     */
    private void appendDet(StringBuilder xml, Invoice invoice) {
        int itemNumber = 1;
        
        for (InvoiceItem item : invoice.items) {
            xml.append("<det nItem=\"").append(itemNumber++).append("\">");
            
            // Produto
            xml.append("<prod>");
            xml.append("<cProd>").append(item.productCode).append("</cProd>");
            xml.append("<cEAN>SEM GTIN</cEAN>");        // Código de barras (ou "SEM GTIN")
            xml.append("<xProd>Produto Teste</xProd>"); // TODO: Adicionar descrição no InvoiceItem
            xml.append("<NCM>12345678</NCM>");          // TODO: Adicionar NCM no InvoiceItem
            xml.append("<CFOP>5102</CFOP>");            // TODO: Adicionar CFOP no InvoiceItem
            xml.append("<uCom>UN</uCom>");              // Unidade comercial
            xml.append("<qCom>").append(item.quantity).append(".0000</qCom>");
            xml.append("<vUnCom>").append(formatDecimal(item.unitPrice)).append("</vUnCom>");
            xml.append("<vProd>").append(formatDecimal(item.totalPrice)).append("</vProd>");
            xml.append("<cEANTrib>SEM GTIN</cEANTrib>");
            xml.append("<uTrib>UN</uTrib>");
            xml.append("<qTrib>").append(item.quantity).append(".0000</qTrib>");
            xml.append("<vUnTrib>").append(formatDecimal(item.unitPrice)).append("</vUnTrib>");
            xml.append("<vOutro>0.00</vOutro>");        // Outras despesas acessórias
            xml.append("<vDesc>0.00</vDesc>");          // Valor do desconto
            xml.append("<indTot>1</indTot>");           // Compõe valor total (1 = Sim)
            xml.append("</prod>");
            
            // Impostos (simplificado)
            appendImpostos(xml, item);
            
            xml.append("</det>");
        }
    }
    
    /**
     * Impostos do produto (ICMS, PIS, COFINS).
     * Implementação simplificada.
     */
    private void appendImpostos(StringBuilder xml, InvoiceItem item) {
        xml.append("<imposto>");
        
        // ICMS
        xml.append("<ICMS>");
        xml.append("<ICMS").append(icmsCst).append(">");  // Tag dinâmica baseada no CST
        xml.append("<orig>").append(icmsOrigem).append("</orig>");
        xml.append("<CST>").append(icmsCst).append("</CST>");
        xml.append("<modBC>3</modBC>");                 // Modalidade BC
        xml.append("<vBC>").append(formatDecimal(item.totalPrice)).append("</vBC>");
        xml.append("<pICMS>").append(icmsAliquota).append("</pICMS>");
        BigDecimal aliqICMS = new BigDecimal(icmsAliquota).divide(new BigDecimal("100"));
        BigDecimal vICMS = item.totalPrice.multiply(aliqICMS);
        xml.append("<vICMS>").append(formatDecimal(vICMS)).append("</vICMS>");
        xml.append("</ICMS").append(icmsCst).append(">");
        xml.append("</ICMS>");
        
        // IPI (OBRIGATÓRIO)
        xml.append("<IPI>");
        xml.append("<cEnq>").append(ipiCenq).append("</cEnq>");
        xml.append("<IPINT>");                         // IPI não tributado
        xml.append("<CST>").append(ipiCst).append("</CST>");
        xml.append("</IPINT>");
        xml.append("</IPI>");
        
        // PIS
        xml.append("<PIS>");
        xml.append("<PISAliq>");
        xml.append("<CST>").append(pisCst).append("</CST>");
        xml.append("<vBC>").append(formatDecimal(item.totalPrice)).append("</vBC>");
        xml.append("<pPIS>").append(pisAliquota).append("</pPIS>");
        BigDecimal aliqPIS = new BigDecimal(pisAliquota).divide(new BigDecimal("100"));
        BigDecimal vPIS = item.totalPrice.multiply(aliqPIS);
        xml.append("<vPIS>").append(formatDecimal(vPIS)).append("</vPIS>");
        xml.append("</PISAliq>");
        xml.append("</PIS>");
        
        // COFINS
        xml.append("<COFINS>");
        xml.append("<COFINSAliq>");
        xml.append("<CST>").append(cofinsCst).append("</CST>");
        xml.append("<vBC>").append(formatDecimal(item.totalPrice)).append("</vBC>");
        xml.append("<pCOFINS>").append(cofinsAliquota).append("</pCOFINS>");
        BigDecimal aliqCOFINS = new BigDecimal(cofinsAliquota).divide(new BigDecimal("100"));
        BigDecimal vCOFINS = item.totalPrice.multiply(aliqCOFINS);
        xml.append("<vCOFINS>").append(formatDecimal(vCOFINS)).append("</vCOFINS>");
        xml.append("</COFINSAliq>");
        xml.append("</COFINS>");
        
        xml.append("</imposto>");
    }
    
    /**
     * Seção <total> - Totalizadores da NF-e.
     */
    private void appendTotal(StringBuilder xml, Invoice invoice) {
        xml.append("<total>");
        xml.append("<ICMSTot>");
        
        // Calcular valores reais dos impostos usando alíquotas configuradas
        BigDecimal aliqICMS = new BigDecimal(icmsAliquota).divide(new BigDecimal("100"));
        BigDecimal aliqPIS = new BigDecimal(pisAliquota).divide(new BigDecimal("100"));
        BigDecimal aliqCOFINS = new BigDecimal(cofinsAliquota).divide(new BigDecimal("100"));
        
        BigDecimal vICMS = invoice.totalAmount.multiply(aliqICMS);
        BigDecimal vPIS = invoice.totalAmount.multiply(aliqPIS);
        BigDecimal vCOFINS = invoice.totalAmount.multiply(aliqCOFINS);
        
        xml.append("<vBC>").append(formatDecimal(invoice.totalAmount)).append("</vBC>");
        xml.append("<vICMS>").append(formatDecimal(vICMS)).append("</vICMS>");
        xml.append("<vICMSDeson>0.00</vICMSDeson>");
        xml.append("<vFCP>0.00</vFCP>");
        xml.append("<vBCST>0.00</vBCST>");
        xml.append("<vST>0.00</vST>");
        xml.append("<vFCPST>0.00</vFCPST>");
        xml.append("<vProd>").append(formatDecimal(invoice.totalAmount)).append("</vProd>");
        xml.append("<vFrete>0.00</vFrete>");
        xml.append("<vSeg>0.00</vSeg>");
        xml.append("<vDesc>0.00</vDesc>");
        xml.append("<vII>0.00</vII>");
        xml.append("<vIPI>0.00</vIPI>");
        xml.append("<vIPIDevol>0.00</vIPIDevol>");
        xml.append("<vPIS>").append(formatDecimal(vPIS)).append("</vPIS>");
        xml.append("<vCOFINS>").append(formatDecimal(vCOFINS)).append("</vCOFINS>");
        xml.append("<vOutro>0.00</vOutro>");
        xml.append("<vNF>").append(formatDecimal(invoice.totalAmount)).append("</vNF>");
        
        xml.append("</ICMSTot>");
        xml.append("</total>");
    }
    
    /**
     * Seção <pag> - Formas de Pagamento.
     */
    private void appendPag(StringBuilder xml, Invoice invoice) {
        xml.append("<pag>");
        xml.append("<detPag>");
        xml.append("<tPag>01</tPag>");                  // Forma pagamento (01 = Dinheiro)
        xml.append("<vPag>").append(formatDecimal(invoice.totalAmount)).append("</vPag>");
        xml.append("</detPag>");
        xml.append("</pag>");
    }
    
    /**
     * Seção <transp> - Informações de Transporte.
     */
    private void appendTransp(StringBuilder xml) {
        xml.append("<transp>");
        xml.append("<modFrete>9</modFrete>");          // 9 = Sem frete
        xml.append("</transp>");
    }
    
    /**
     * Seção <infAdic> - Informações Adicionais.
     */
    private void appendInfAdic(StringBuilder xml) {
        xml.append("<infAdic>");
        xml.append("<infCpl>Nota Fiscal emitida em ambiente de homologacao - Sem valor fiscal</infCpl>");
        xml.append("</infAdic>");
    }
    
    /**
     * Formata data/hora para padrão ISO 8601.
     */
    private String formatDateTime(java.time.LocalDateTime dateTime) {
        return dateTime.atZone(java.time.ZoneId.of("America/Sao_Paulo"))
                       .format(DATE_TIME_FORMATTER);
    }
    
    /**
     * Formata BigDecimal para XML (2 casas decimais).
     */
    private String formatDecimal(BigDecimal value) {
        return String.format("%.2f", value);
    }
    
    /**
     * Escapa caracteres especiais para XML.
     */
    private String escapeXml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&apos;");
    }
}
