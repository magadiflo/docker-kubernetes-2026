package dev.magadiflo.gateway.app.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

@Slf4j
@RestController
@RequestMapping(path = "/test/oauth2-client")
public class TestOAuth2ClientController {

    // El registrationId debe coincidir exactamente con el registro definido en application.yml
    @GetMapping(path = "/authorized-info")
    public Mono<ResponseEntity<Map<String, Object>>> authorizedInfo(
            @RegisteredOAuth2AuthorizedClient(registrationId = "gateway-client-registration") OAuth2AuthorizedClient authorizedClient) {

        // LinkedHashMap conserva el orden de inserción (facilitando la lectura del JSON)
        // y, a diferencia de Map.of(...), permite almacenar valores nulos.
        Map<String, Object> accessTokenMap = new LinkedHashMap<>();
        accessTokenMap.put("access_token", authorizedClient.getAccessToken().getTokenValue());
        accessTokenMap.put("tokenType", authorizedClient.getAccessToken().getTokenType().getValue());
        accessTokenMap.put("issuedAt", authorizedClient.getAccessToken().getIssuedAt());
        accessTokenMap.put("expiresAt", authorizedClient.getAccessToken().getExpiresAt());
        accessTokenMap.put("scopes", authorizedClient.getAccessToken().getScopes());

        // En este proyecto siempre esperamos recibir un refresh_token porque registramos el
        // AuthorizationGrantType.REFRESH_TOKEN. Sin embargo, otros flujos OAuth2 o configuraciones
        // podrían no emitirlo, por lo que protegemos el acceso para evitar un NullPointerException
        // y mantener este código compatible con futuras configuraciones del cliente OAuth2.
        Map<String, Object> refreshTokenMap = new LinkedHashMap<>();
        OAuth2RefreshToken refreshToken = authorizedClient.getRefreshToken();
        refreshTokenMap.put("refresh_token", Objects.nonNull(refreshToken) ? refreshToken.getTokenValue() : null);
        refreshTokenMap.put("issuedAt", Objects.nonNull(refreshToken) ? refreshToken.getIssuedAt() : null);
        refreshTokenMap.put("expiresAt", Objects.nonNull(refreshToken) ? refreshToken.getExpiresAt() : null);

        Map<String, Object> authorizedInfo = new LinkedHashMap<>();
        authorizedInfo.put("registrationId", authorizedClient.getClientRegistration().getRegistrationId());
        authorizedInfo.put("clientId", authorizedClient.getClientRegistration().getClientId());
        authorizedInfo.put("principalName", authorizedClient.getPrincipalName());
        authorizedInfo.put("accessToken", accessTokenMap);
        authorizedInfo.put("refreshToken", refreshTokenMap);

        log.info("Información del OAuth2AuthorizedClient recuperada correctamente: {}", authorizedInfo);

        return Mono.just(ResponseEntity.ok(authorizedInfo));
    }
}
