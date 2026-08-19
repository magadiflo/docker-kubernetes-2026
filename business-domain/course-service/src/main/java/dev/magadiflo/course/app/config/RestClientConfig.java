package dev.magadiflo.course.app.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.restclient.autoconfigure.RestClientBuilderConfigurer;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.client.RestClient;

import java.util.Objects;

@Configuration
public class RestClientConfig {

    @Value("${custom.user-service.base-url}")
    private String userServiceBaseUrl;

    @LoadBalanced
    @Bean
    public RestClient.Builder restClientBuilder(RestClientBuilderConfigurer configurer) {
        return configurer.configure(RestClient.builder());
    }

    @Bean(name = "userRestClient")
    public RestClient userServiceRestClient(@Qualifier("restClientBuilder") RestClient.Builder builder) {
        return builder
                .baseUrl(this.userServiceBaseUrl)
                .requestInterceptor(this.bearerTokenPropagationInterceptor())
                .build();
    }

    private ClientHttpRequestInterceptor bearerTokenPropagationInterceptor() {
        return (request, body, execution) -> {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (Objects.nonNull(authentication) && authentication.getCredentials() instanceof Jwt jwt) {
                request.getHeaders().setBearerAuth(jwt.getTokenValue());
            }

            return execution.execute(request, body);
        };
    }
}
