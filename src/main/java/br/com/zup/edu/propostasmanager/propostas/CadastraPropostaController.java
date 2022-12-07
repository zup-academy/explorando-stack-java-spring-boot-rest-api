package br.com.zup.edu.propostasmanager.propostas;

import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import javax.validation.Valid;
import java.net.URI;

@RestController
public class CadastraPropostaController {
    private final PropostaRepository repository;

    public CadastraPropostaController(PropostaRepository repository) {
        this.repository = repository;
    }

    @PostMapping("/api/v1/propostas")
    @Transactional
    public ResponseEntity<?> cadastrar(@RequestBody @Valid PropostaRequest request, UriComponentsBuilder uriBuilder){

        Proposta proposta = request.toModel(repository);

        repository.save(proposta);

        URI location = uriBuilder.path("/api/v1/propostas/{id}")
                .buildAndExpand(proposta.getId())
                .toUri();

        return ResponseEntity.created(location).build();
    }
}
