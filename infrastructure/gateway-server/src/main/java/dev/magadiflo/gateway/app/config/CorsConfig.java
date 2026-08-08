package dev.magadiflo.gateway.app.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.time.Duration;
import java.util.List;

/**
 * Configuración global de CORS (Cross-Origin Resource Sharing) para el API Gateway Reactivo (WebFlux).
 * <p>
 * Centraliza las políticas de acceso entre orígenes para asegurar que la aplicación Frontend (Angular)
 * pueda comunicarse libremente con el backend a través del Gateway sin ser bloqueada por el navegador.
 * <p>
 * Se expone como {@link CorsConfigurationSource} (y no como {@link org.springframework.web.cors.reactive.CorsWebFilter})
 * porque este es el tipo que Spring Security espera para integrar CORS dentro de su propia cadena de
 * seguridad, garantizando que las peticiones preflight (OPTIONS) se resuelvan antes de evaluar
 * cualquier regla de autorización.
 */
@Configuration
public class CorsConfig {

    @Value("${custom.frontend.angular.base-url}")
    private String frontendAngularBaseUrl;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration corsConfig = new CorsConfiguration();

        // Especifica el origen explícito permitido para comunicarse con este Gateway (URL de Angular).
        // IMPORTANTE: Cuando allowCredentials es 'true', la especificación de W3C prohíbe el uso de wildcard "*".
        corsConfig.setAllowedOrigins(List.of(this.frontendAngularBaseUrl));

        // Define los verbos HTTP permitidos para consumir la API desde el cliente
        corsConfig.setAllowedMethods(List.of(
                HttpMethod.GET.name(),
                HttpMethod.POST.name(),
                HttpMethod.PUT.name(),
                HttpMethod.DELETE.name(),
                HttpMethod.PATCH.name(),
                HttpMethod.OPTIONS.name()
        ));

        // Cabeceras que el navegador podrá enviar durante una petición CORS
        corsConfig.setAllowedHeaders(List.of(
                HttpHeaders.AUTHORIZATION,  // Permitido para enviar tokens Bearer / Credenciales
                HttpHeaders.CONTENT_TYPE,       // Permitido para enviar payloads JSON ('application/json')
                HttpHeaders.ACCEPT,             // Permitido para solicitar tipos de respuesta explícitos
                "X-Requested-With"              // Permitido para identificadores de peticiones AJAX (si los usas)
        ));

        // Permite que el navegador envíe credenciales (en nuestro caso una cookie de
        // sesión HTTP-Only (SESSION)) en las peticiones cross-origin. Es indispensable en nuestro patrón BFF, ya que Angular
        // autentica al usuario mediante la cookie HttpOnly SESSION emitida por el Gateway. Si este valor fuera false, el
        // navegador nunca enviaría dicha cookie y todas las peticiones llegarían al Gateway como anónimas
        corsConfig.setAllowCredentials(true);

        // El navegador almacenará en caché la respuesta del preflight (OPTIONS)
        // durante una hora, evitando repetir esa comprobación en cada petición.
        corsConfig.setMaxAge(Duration.ofHours(1));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // Aplica esta configuración de CORS a todas las rutas expuestas por el Gateway
        source.registerCorsConfiguration("/**", corsConfig);

        return source;
    }
}
