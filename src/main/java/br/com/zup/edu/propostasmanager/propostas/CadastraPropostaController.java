package br.com.zup.edu.propostasmanager.propostas;

import br.com.zup.edu.propostasmanager.propostas.clients.AnaliseFinanceiraClient;
import br.com.zup.edu.propostasmanager.propostas.clients.AnaliseReponse;
import br.com.zup.edu.propostasmanager.propostas.clients.SolicitaAnaliseRequest;
import feign.FeignException;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import javax.validation.Valid;
import java.net.URI;

import static br.com.zup.edu.propostasmanager.propostas.validators.StatusProposta.ELEGIVEL;
import static br.com.zup.edu.propostasmanager.propostas.validators.StatusProposta.NAO_ELEGIVEL;

@RestController
public class CadastraPropostaController {
    private final PropostaRepository repository;
    private final AnaliseFinanceiraClient analiseFinanceiraClient;

    public CadastraPropostaController(PropostaRepository repository, AnaliseFinanceiraClient analiseFinanceiraClient) {
        this.repository = repository;
        this.analiseFinanceiraClient = analiseFinanceiraClient;
    }

    @PostMapping("/api/v1/propostas")
    @Transactional
    public ResponseEntity<?> cadastrar(@RequestBody @Valid PropostaRequest request, UriComponentsBuilder uriBuilder) {

        Proposta proposta = request.toModel(repository);

        repository.save(proposta);

        try {

            ResponseEntity<AnaliseReponse> analise = analiseFinanceiraClient.solicita(
                    new SolicitaAnaliseRequest(proposta)
            );
            proposta.setStatus(ELEGIVEL);

        } catch (FeignException.UnprocessableEntity ex) {
            proposta.setStatus(NAO_ELEGIVEL);
        }

        URI location = uriBuilder.path("/api/v1/propostas/{id}")
                .buildAndExpand(proposta.getId())
                .toUri();

        return ResponseEntity.created(location).build();
    }
}
