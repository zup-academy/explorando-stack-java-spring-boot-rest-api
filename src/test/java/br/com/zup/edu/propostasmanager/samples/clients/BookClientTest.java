package br.com.zup.edu.propostasmanager.samples.clients;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static java.lang.String.format;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureWireMock(port = 19090)
class BookClientTest {
    @Autowired
    private ObjectMapper mapper;
    @Autowired
    private BookClient client;


    @Test
    @DisplayName("must return the book by id")
    void t1() throws JsonProcessingException {
        UUID bookId = UUID.randomUUID();
        BookResponse bookResponse = new BookResponse(bookId, "Domain Drive Design", "DDD", BigDecimal.TEN);
        String payload = mapper.writeValueAsString(bookResponse);
        String url = format("/api/books/%s", bookId);

        stubFor(
                get(
                        urlEqualTo(url)
                ).willReturn(
                        aResponse()
                                .withStatus(200)
                                .withHeader("Content-Type", "application/json")
                                .withBody(payload)
                )
        );

        ResponseEntity<BookResponse> response = client.getById(bookId);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());
        assertEquals(bookResponse, response.getBody());

    }

    @Test
    @DisplayName("should not return book by id")
    void t2() throws JsonProcessingException {
        UUID bookId = UUID.randomUUID();
        Map<String, String> body = Map.of("message", "There is no book for the given id");
        String payload = mapper.writeValueAsString(body);
        String url = format("/api/books/%s", bookId);

        stubFor(
                get(urlEqualTo(url))
                        .willReturn(
                                aResponse()
                                        .withStatus(404)
                                        .withHeader("Content-Type", "application/json")
                                        .withBody(payload)
                        )
        );

        assertThrows(FeignException.NotFound.class, () -> {
            ResponseEntity<BookResponse> response = client.getById(bookId);
            assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
            assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());
        });

    }


    @Test
    @DisplayName("must register a book")
    void t3() throws URISyntaxException {
        String location = format("http://locahost/api/books/%s", UUID.randomUUID());
        stubFor(
                post(urlEqualTo("/api/books"))
                        .willReturn(
                                aResponse()
                                        .withStatus(201)
                                        .withHeader("Content-Type", "application/json")
                                        .withHeader("location", location)

                        )
        );

        BookRequest bookRequest = new BookRequest("Clean Code", "Code Clean", BigDecimal.TEN);
        ResponseEntity<?> response = client.registerThebook(bookRequest);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(new URI(location), response.getHeaders().getLocation());

    }

    @Test
    @DisplayName("You must not register a book with a null title")
    void t4() throws JsonProcessingException {
        Map<String, String> body = Map.of("message", "Unable to register a book with null title");
        String payload = mapper.writeValueAsString(body);
        stubFor(
                post(urlEqualTo("/api/books"))
                        .willReturn(
                                aResponse()
                                        .withStatus(400)
                                        .withHeader("Content-Type", "application/json")
                                        .withBody(payload)

                        )
        );
        BookRequest bookRequest = new BookRequest(null, "Code Clean", BigDecimal.TEN);
        assertThrows(FeignException.BadRequest.class, () -> {
            ResponseEntity<?> response = client.registerThebook(bookRequest);
            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
            assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());
        });
    }

    @Test
    @DisplayName("should not register a book with a title already registered")
    void t5() throws JsonProcessingException {
        Map<String, String> body = Map.of("message", "There is already a book registration for this title");
        String payload = mapper.writeValueAsString(body);
        stubFor(
                post(urlEqualTo("/api/books"))
                        .willReturn(
                                aResponse()
                                        .withStatus(422)
                                        .withHeader("Content-Type", "application/json")
                                        .withBody(payload)

                        )
        );


        BookRequest bookRequest = new BookRequest("Clean Code", "Code Clean", BigDecimal.TEN);
        assertThrows(FeignException.UnprocessableEntity.class, () -> {
            ResponseEntity<?> response = client.registerThebook(bookRequest);
            assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.getStatusCode());
            assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());
        });

    }
}