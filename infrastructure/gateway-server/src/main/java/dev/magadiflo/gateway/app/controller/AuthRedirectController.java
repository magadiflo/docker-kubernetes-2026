package dev.magadiflo.gateway.app.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.net.URI;

@RestController
public class AuthRedirectController {

    @Value("${custom.frontend.angular.base-url}")
    private String frontendAngularBaseUrl;

    /**
     * Endpoint al que el authorization-server redirige luego de finalizar la sesión.
     * Este endpoint, a su vez, redirige hacia la URL de Angular configurada en el application.yml.
     */
    @GetMapping(path = "/post-logout")
    public Mono<ResponseEntity<Void>> handlePostLogout() {
        return Mono.just(
                ResponseEntity
                        .status(HttpStatus.FOUND)
                        .location(URI.create(this.frontendAngularBaseUrl))
                        .build()
        );
    }
}
