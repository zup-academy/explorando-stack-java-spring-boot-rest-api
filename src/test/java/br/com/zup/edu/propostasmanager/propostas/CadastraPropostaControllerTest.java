package br.com.zup.edu.propostasmanager.propostas;

import br.com.zup.edu.propostasmanager.propostas.clients.AnaliseReponse;
import br.com.zup.edu.propostasmanager.propostas.validators.StatusProposta;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.zalando.problem.spring.common.MediaTypes;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc(printOnlyOnFailure = false)
@AutoConfigureWireMock(port = 19090)
class CadastraPropostaControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper mapper;
    @Autowired
    private PropostaRepository repository;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
    }

    @Test
    @DisplayName("deve criar uma proposta elegivel")
    void t1() throws Exception {
        //cenario

        PropostaRequest propostaRequest = new PropostaRequest(
                "127.155.130-62",
                "jordi@email.com",
                "Jordi H",
                "rua machado de assis 89- Ibia MG",
                BigDecimal.ONE
        );

        AnaliseReponse analiseReponse = new AnaliseReponse(
                propostaRequest.getDocumento(),
                propostaRequest.getNome(),
                null,
                "SEM_RESTRICAO"
        );

        String payloadResponseAnaliseFinanceira = mapper.writeValueAsString(analiseReponse);

        stubFor(
                post(
                        urlEqualTo("/api/solicitacao")
                ).willReturn(
                        aResponse()
                                .withStatus(201)
                                .withHeader("Content-Type", "application/json")
                                .withBody(payloadResponseAnaliseFinanceira)
                )
        );

        String payload = mapper.writeValueAsString(propostaRequest);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post("/api/v1/propostas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload);

        //acao
        ResultActions response = mockMvc.perform(request);

        // validacao
        response.andExpectAll(
                status().isCreated(),
                redirectedUrlPattern("**/api/v1/propostas/*")
        );

        List<Proposta> propostas = repository.findAll();
        assertEquals(1, propostas.size());
        assertEquals(StatusProposta.ELEGIVEL,propostas.get(0).getStatus());
    }




    @Test
    @DisplayName("deve criar uma proposta nao elegivel")
    void t2() throws Exception {
        //cenario

        PropostaRequest propostaRequest = new PropostaRequest(
                "344.279.840-02",
                "jordi@email.com",
                "Jordi H",
                "rua machado de assis 89- Ibia MG",
                BigDecimal.ONE
        );

        AnaliseReponse analiseReponse = new AnaliseReponse(
                propostaRequest.getDocumento(),
                propostaRequest.getNome(),
                null,
                "COM_RESTRICAO"
        );

        String payloadResponseAnaliseFinanceira = mapper.writeValueAsString(analiseReponse);

        stubFor(
                post(
                        urlEqualTo("/api/solicitacao")
                ).willReturn(
                        aResponse()
                                .withStatus(422)
                                .withHeader("Content-Type", "application/json")
                                .withBody(payloadResponseAnaliseFinanceira)
                )
        );

        String payload = mapper.writeValueAsString(propostaRequest);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post("/api/v1/propostas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload);

        //acao
        ResultActions response = mockMvc.perform(request);

        // validacao
        response.andExpectAll(
                status().isCreated(),
                redirectedUrlPattern("**/api/v1/propostas/*")
        );

        List<Proposta> propostas = repository.findAll();
        assertEquals(1, propostas.size());
        assertEquals(StatusProposta.NAO_ELEGIVEL,propostas.get(0).getStatus());
    }


    @Test
    @DisplayName("nao deve criar uma proposta invalida")
    void t3() throws Exception {
        //cenario
        PropostaRequest propostaRequest = new PropostaRequest(
                null,
                null,
                null,
                null,
                null
        );


        String payload = mapper.writeValueAsString(propostaRequest);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post("/api/v1/propostas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload);

        //acao
        ResultActions response = mockMvc.perform(request);

        // validacao

        response.andExpectAll(
                status().isBadRequest(),
                header().string(HttpHeaders.CONTENT_TYPE, is(MediaTypes.PROBLEM_VALUE)),
                jsonPath("$.type", is("https://zalando.github.io/problem/constraint-violation")),
                jsonPath("$.title", is("Constraint Violation")),
                jsonPath("$.status", is(400)),
                jsonPath("$.violations", hasSize(5)),
                jsonPath("$.violations", containsInAnyOrder(
                                violation("nome", "must not be blank"),
                                violation("email", "must not be blank"),
                                violation("documento", "must not be blank"),
                                violation("endereco", "must not be blank"),
                                violation("salario", "must not be null")
                        )
                )
        );

    }

    private Map<String, Object> violation(String field, String message) {
        return Map.of(
                "field", field,
                "message", message
        );
    }

}