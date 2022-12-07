package br.com.zup.edu.propostasmanager.samples.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.UUID;

@FeignClient(name = "bookClient", url = "${api.book.url}")
public interface BookClient {

    @GetMapping("/{id}")
    ResponseEntity<BookResponse> getById(@PathVariable UUID id);

    @PostMapping
    ResponseEntity<?> registerThebook(@RequestBody BookRequest request);
}
