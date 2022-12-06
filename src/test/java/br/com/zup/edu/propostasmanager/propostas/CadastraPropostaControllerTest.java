package br.com.zup.edu.propostasmanager.propostas;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.zalando.problem.spring.common.MediaTypes;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc(printOnlyOnFailure = false)
@ActiveProfiles("test")
class CadastraPropostaControllerTest {
    @Autowired
    private PropostaRepository repository;
    @Autowired
    private ObjectMapper mapper;
    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
    }

    @Test
    @DisplayName("deve cadastrar uma proposta")
    void t1() throws Exception {
        //cenario
        PropostaRequest propostaRequest = new PropostaRequest(
                "338.219.110-50",
                "jordi.silva@zup.com",
                "Jordi H Silva",
                "Rua Machado de Assis 134, Jardim Europa - Ibia MG",
                BigDecimal.ONE
        );

        String payload = mapper.writeValueAsString(propostaRequest);


        MockHttpServletRequestBuilder request = post("/api/v1/propostas")
                .contentType(MediaType.APPLICATION_JSON)
                .header("accept-language", "pt-br")
                .content(payload);

        //acao
        ResultActions response = mockMvc.perform(request);

        //validacao
        String location = response.andExpectAll(
                        status().isCreated(),
                        redirectedUrlPattern("**/propostas/*")
                )
                .andReturn()
                .getResponse()
                .getHeader("location");

        assertNotNull(location);
        int lastIndexOf = location.lastIndexOf("/");
        assertNotEquals(-1, lastIndexOf);
        String id = location.substring(lastIndexOf + 1);

        assertTrue(repository.existsById(Long.valueOf(id)));

    }


    @Test
    @DisplayName("nao deve cadastrar uma proposta com dados invalidos")
    void t2() throws Exception {
        //cenario

        PropostaRequest propostaRequest = new PropostaRequest(
                null,
                null,
                null,
                null,
                null
        );

        String payload = mapper.writeValueAsString(propostaRequest);


        MockHttpServletRequestBuilder request = post("/api/v1/propostas")
                .contentType(MediaType.APPLICATION_JSON)
                .header("accept-language", "en")
                .content(payload);
        //acao
        ResultActions response = mockMvc.perform(request);

        //validacao
        response.andExpectAll(
                header().string(HttpHeaders.CONTENT_TYPE, is(MediaTypes.PROBLEM_VALUE)),
                jsonPath("$.type", is("https://zalando.github.io/problem/constraint-violation")),
                jsonPath("$.title", is("Constraint Violation")),
                jsonPath("$.status", is(400)),
                jsonPath("$.violations", hasSize(5)),
                jsonPath("$.violations", containsInAnyOrder(
                                violation("endereco", "must not be blank"),
                                violation("documento", "must not be blank"),
                                violation("email", "must not be blank"),
                                violation("nome", "must not be blank"),
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