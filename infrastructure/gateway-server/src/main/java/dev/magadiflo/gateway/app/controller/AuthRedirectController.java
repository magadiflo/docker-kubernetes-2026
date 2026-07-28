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

    private static final String OAUTH2_AUTHORIZATION_URI = "/oauth2/authorization/gateway-client-registration";

    @Value("${custom.frontend.angular.base-url}")
    private String frontendAngularBaseUrl;

    /**
     * Endpoint neutral de entrada para iniciar sesión.
     * Angular solo conoce esta ruta, sin acoplarse al registrationId ni a detalles de OAuth2.
     */
    @GetMapping(path = "/auth/login")
    public Mono<ResponseEntity<Void>> handleLogin() {
        return Mono.just(
                ResponseEntity
                        .status(HttpStatus.FOUND)
                        .location(URI.create(OAUTH2_AUTHORIZATION_URI))
                        .build()
        );
    }

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
