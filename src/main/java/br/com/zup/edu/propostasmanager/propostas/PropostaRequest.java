package br.com.zup.edu.propostasmanager.propostas;

import br.com.zup.edu.propostasmanager.propostas.validators.DocumentoValid;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.math.BigDecimal;

import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;

public class PropostaRequest {
    @DocumentoValid
    @NotBlank
    private String documento;
    @NotBlank
    @Email
    private String email;
    @NotBlank
    private String nome;
    @NotBlank
    private String endereco;
    @NotNull
    @Positive
    private BigDecimal salario;

    public PropostaRequest(String documento, String email, String nome, String endereco, BigDecimal salario) {
        this.documento = documento;
        this.email = email;
        this.nome = nome;
        this.endereco = endereco;
        this.salario = salario;
    }

    public Proposta toModel(PropostaRepository repository){
        if (repository.existsByDocumento(documento)) {
            throw new ResponseStatusException(UNPROCESSABLE_ENTITY,"Já existe uma proposta para este documento.");
        }

        return new Proposta(documento,email,nome,endereco,salario);
    }


    public String getDocumento() {
        return documento;
    }

    public String getEmail() {
        return email;
    }

    public String getNome() {
        return nome;
    }

    public String getEndereco() {
        return endereco;
    }

    public BigDecimal getSalario() {
        return salario;
    }
}
