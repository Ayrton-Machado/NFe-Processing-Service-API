package com.erpservices.nfe.fiscal.envio;

import br.com.swconsultoria.nfe.Nfe;
import br.com.swconsultoria.nfe.dom.ConfiguracoesNfe;
import br.com.swconsultoria.nfe.dom.enuns.DocumentoEnum;
import br.com.swconsultoria.nfe.dom.enuns.StatusEnum;
import br.com.swconsultoria.nfe.schema_4.enviNFe.TEnviNFe;
import br.com.swconsultoria.nfe.schema_4.enviNFe.TNFe;
import br.com.swconsultoria.nfe.schema_4.enviNFe.TRetEnviNFe;
import br.com.swconsultoria.nfe.util.ConstantesUtil;
import br.com.swconsultoria.nfe.util.RetornoUtil;
import br.com.swconsultoria.nfe.util.XmlNfeUtil;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class SendNfe {
    
    public void send(TNFe nfe, ConfiguracoesNfe configuracoesNfe) throws Exception {

        //Monta EnviNfe
        TEnviNFe enviNFe = new TEnviNFe();
        enviNFe.setVersao(ConstantesUtil.VERSAO.NFE);
        enviNFe.setIdLote("1");
        enviNFe.setIndSinc("1");
        enviNFe.getNFe().add(nfe);
        
        // Monta e Assina o XML
        enviNFe = Nfe.montaNfe(configuracoesNfe, enviNFe, true);

        // Envia a Nfe para a Sefaz
        TRetEnviNFe retorno = Nfe.enviarNfe(configuracoesNfe, enviNFe, DocumentoEnum.NFE);

        //Valida se o Retorno é Assincrono
        if (RetornoUtil.isRetornoAssincrono(retorno)) {
            //Pega o Recibo
            String recibo = retorno.getInfRec().getNRec();
            int tentativa = 0;
            br.com.swconsultoria.nfe.schema_4.consReciNFe.TRetConsReciNFe retornoNfe = null;

            //Define Numero de tentativas que irá tentar a Consulta
            while (tentativa < 15) {
                retornoNfe = Nfe.consultaRecibo(configuracoesNfe, recibo, DocumentoEnum.NFE);
                if (retornoNfe.getCStat().equals(StatusEnum.LOTE_EM_PROCESSAMENTO.getCodigo())) {
                    System.out.println("INFO: Lote Em Processamento, vai tentar novamente apos 1 Segundo.");
                    Thread.sleep(1000);
                    tentativa++;
                } else {
                    break;
                }
            }

            RetornoUtil.validaAssincrono(retornoNfe);
            System.out.println();
            System.out.println("# Status: " + retornoNfe.getProtNFe().get(0).getInfProt().getCStat() + " - " + retornoNfe.getProtNFe().get(0).getInfProt().getXMotivo());
            System.out.println("# Protocolo: " + retornoNfe.getProtNFe().get(0).getInfProt().getNProt());
            System.out.println("# XML Final: " + XmlNfeUtil.criaNfeProc(enviNFe, retornoNfe.getProtNFe().get(0)));

        } else {
            //Se for else o Retorno é Sincrono

            //Valida Retorno Sincrono
            RetornoUtil.validaSincrono(retorno);
            System.out.println();
            System.out.println("# Status: " + retorno.getProtNFe().getInfProt().getCStat() + " - " + retorno.getProtNFe().getInfProt().getXMotivo());
            System.out.println("# Protocolo: " + retorno.getProtNFe().getInfProt().getNProt());
            System.out.println("# Xml Final :" + XmlNfeUtil.criaNfeProc(enviNFe, retorno.getProtNFe()));
        }    
    }
    
}
