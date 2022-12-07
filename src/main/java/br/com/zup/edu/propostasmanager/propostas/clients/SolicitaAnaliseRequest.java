package br.com.zup.edu.propostasmanager.propostas.clients;

import br.com.zup.edu.propostasmanager.propostas.Proposta;

public class SolicitaAnaliseRequest {
    private String documento;
    private String nome;
    private String idProposta;


    public SolicitaAnaliseRequest(Proposta proposta) {
        this.documento = proposta.getDocumento();
        this.nome = proposta.getNome();
        this.idProposta = proposta.getId().toString();
    }

    public SolicitaAnaliseRequest() {
    }

    public String getDocumento() {
        return documento;
    }

    public String getNome() {
        return nome;
    }

    public String getIdProposta() {
        return idProposta;
    }
}
