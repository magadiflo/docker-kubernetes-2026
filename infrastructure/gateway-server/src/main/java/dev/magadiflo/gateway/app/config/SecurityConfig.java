package dev.magadiflo.gateway.app.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtGrantedAuthoritiesConverterAdapter;
import org.springframework.security.web.server.SecurityWebFilterChain;
import reactor.core.publisher.Mono;

@Configuration
public class SecurityConfig {
    @Bean
    public SecurityWebFilterChain webFilterChain(ServerHttpSecurity http) {
        http
                .authorizeExchange(authorize -> authorize
                        .pathMatchers("/actuator/**").permitAll()
                        .pathMatchers(HttpMethod.GET, "/api/v1/courses", "/api/v1/users", "/api/v1/users/info").permitAll()
                        .pathMatchers(HttpMethod.GET, "/api/v1/courses/{courseId}", "/api/v1/users/{userId}").hasAnyRole("USER", "ADMIN")
                        .pathMatchers("/api/v1/courses/**", "/api/v1/users/**").hasRole("ADMIN")
                        .anyExchange().authenticated()
                )
                .csrf(ServerHttpSecurity.CsrfSpec::disable)

                // Configuramos este microservicio como Resource Server (validará tokens JWT emitidos por el Authorization Server)
                // y aplicamos nuestro convertidor personalizado para mapear los claims del JWT a roles/authorities de Spring Security.
                .oauth2ResourceServer(oauth2ResourceServer ->
                        oauth2ResourceServer.jwt(jwt ->
                                jwt.jwtAuthenticationConverter(this.jwtAuthenticationConverter()))
                );

        return http.build();
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
