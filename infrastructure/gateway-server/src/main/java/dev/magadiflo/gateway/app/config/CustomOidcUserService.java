package dev.magadiflo.gateway.app.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcReactiveOAuth2UserService;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CustomOidcUserService extends OidcReactiveOAuth2UserService {

    private static final String ROLES_CLAIM = "roles";

    @Override
    public Mono<OidcUser> loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        return super.loadUser(userRequest)
                .map(this::enrichWithRolesFromIdToken);
    }

    private OidcUser enrichWithRolesFromIdToken(OidcUser oidcUser) {
        Set<GrantedAuthority> defaultOidcAuthorities = new HashSet<>(oidcUser.getAuthorities());
        log.info("Authorities por defecto que trae el OidcUser (scopes, OIDC_USER, etc.): {}", defaultOidcAuthorities);

        List<String> rolesFromIdToken = oidcUser.getIdToken().getClaimAsStringList(ROLES_CLAIM);
        log.info("Roles leídos del claim '{}' en el id_token: {}", ROLES_CLAIM, rolesFromIdToken);

        if (Objects.isNull(rolesFromIdToken) || rolesFromIdToken.isEmpty()) {
            return oidcUser;
        }

        Set<GrantedAuthority> roleBasedAuthorities = rolesFromIdToken.stream()
                .map(SimpleGrantedAuthority::new) // ya vienen como "ROLE_ADMIN", "ROLE_USER"
                .collect(Collectors.toSet());

        roleBasedAuthorities.addAll(defaultOidcAuthorities);
        log.info("Authorities finales asignadas al OidcUser: {}", roleBasedAuthorities);

        return new DefaultOidcUser(
                roleBasedAuthorities,
                oidcUser.getIdToken(),
                oidcUser.getUserInfo()
        );
    }
}
