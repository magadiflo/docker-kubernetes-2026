package dev.magadiflo.gateway.app.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/api/users")
public class UserInfoController {

    @GetMapping(path = "/me")
    public Mono<ResponseEntity<Map<String, Object>>> getCurrentUser(@AuthenticationPrincipal OidcUser oidcUser) {
        if (Objects.isNull(oidcUser)) {
            return Mono.just(ResponseEntity.ok(Map.of("authenticated", false)));
        }

        List<String> roles = oidcUser.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        String username = Objects.nonNull(oidcUser.getPreferredUsername())
                ? oidcUser.getPreferredUsername()
                : oidcUser.getSubject();

        return Mono.just(ResponseEntity.ok(
                Map.of(
                        "authenticated", true,
                        "username", username,
                        "roles", roles
                )));
    }
}
