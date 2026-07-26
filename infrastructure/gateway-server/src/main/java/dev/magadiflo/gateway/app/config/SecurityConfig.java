package dev.magadiflo.gateway.app.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.client.oidc.web.server.logout.OidcClientInitiatedServerLogoutSuccessHandler;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtGrantedAuthoritiesConverterAdapter;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.logout.ServerLogoutSuccessHandler;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Configuration
public class SecurityConfig {

    private final ReactiveClientRegistrationRepository clientRegistrationRepository;

    @Bean
    public SecurityWebFilterChain webFilterChain(ServerHttpSecurity http) {
        http
                .authorizeExchange(authorize -> authorize
                        .pathMatchers("/actuator/**", "/post-logout").permitAll()
                        .pathMatchers(HttpMethod.GET, "/api/v1/courses", "/api/v1/users/by-ids", "/api/v1/users/info").permitAll()
                        .pathMatchers(HttpMethod.GET, "/api/v1/courses/{courseId}", "/api/v1/users/{userId}").hasRole("USER")
                        .pathMatchers(HttpMethod.GET, "/api/v1/users").hasAnyRole("USER", "ADMIN")
                        .pathMatchers(HttpMethod.GET, "/api/v1/courses/load-balancer-test", "/api/v1/users/simulate-error").hasRole("ADMIN")
                        .pathMatchers("/api/v1/courses/**", "/api/v1/users/**").hasRole("ADMIN")
                        .anyExchange().authenticated()
                )
                .csrf(ServerHttpSecurity.CsrfSpec::disable)

                // Habilita el flujo de inicio de sesión OAuth 2.1 (Authorization Code)
                .oauth2Login(Customizer.withDefaults())

                // Habilita la infraestructura para actuar como Cliente OAuth 2.1
                .oauth2Client(Customizer.withDefaults())

                // Configuramos este microservicio como Resource Server (validará tokens JWT emitidos por el Authorization Server)
                // y aplicamos nuestro convertidor personalizado para mapear los claims del JWT a roles/authorities de Spring Security.
                .oauth2ResourceServer(oauth2ResourceServer ->
                        oauth2ResourceServer.jwt(jwt ->
                                jwt.jwtAuthenticationConverter(this.jwtAuthenticationConverter()))
                )
                .logout(logout -> logout
                        .logoutSuccessHandler(this.oidcLogoutSuccessHandler()));

        return http.build();
    }

    private ServerLogoutSuccessHandler oidcLogoutSuccessHandler() {
        OidcClientInitiatedServerLogoutSuccessHandler oidcLogoutSuccessHandler =
                new OidcClientInitiatedServerLogoutSuccessHandler(this.clientRegistrationRepository);

        // A dónde debe redirigir el authorization-server luego de cerrar sesión allí.
        // {baseUrl} se resuelve como http://localhost:8090 (la dirección de este mismo gateway-server)
        oidcLogoutSuccessHandler.setPostLogoutRedirectUri("{baseUrl}/post-logout");
        return oidcLogoutSuccessHandler;
    }

    private Converter<Jwt, Mono<AbstractAuthenticationToken>> jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        //Establece el nombre del claim del token que utilizará este convertidor para mapear las autoridades
        grantedAuthoritiesConverter.setAuthoritiesClaimName("roles");

        // Evitamos que Spring agregue automáticamente el prefijo "SCOPE_"
        // porque nuestros roles ya vienen como ROLE_ADMIN, ROLE_USER, etc.
        grantedAuthoritiesConverter.setAuthorityPrefix("");

        ReactiveJwtGrantedAuthoritiesConverterAdapter authoritiesConverter =
                new ReactiveJwtGrantedAuthoritiesConverterAdapter(grantedAuthoritiesConverter);

        ReactiveJwtAuthenticationConverter jwtAuthenticationConverter = new ReactiveJwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(authoritiesConverter);

        return jwtAuthenticationConverter;
    }
}
