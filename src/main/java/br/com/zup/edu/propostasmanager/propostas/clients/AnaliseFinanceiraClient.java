package br.com.zup.edu.propostasmanager.propostas.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "analiseFinanceiraClient", url = "${api.financeiro.url}")
public interface AnaliseFinanceiraClient {

    @PostMapping("/api/solicitacao")
    ResponseEntity<AnaliseReponse> solicita(@RequestBody SolicitaAnaliseRequest analiseRequest);
}
