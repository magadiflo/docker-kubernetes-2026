package dev.magadiflo.user.app.controller;

import dev.magadiflo.user.app.dto.UserRequest;
import dev.magadiflo.user.app.dto.UserResponse;
import dev.magadiflo.user.app.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(path = "/api/{version}/users", version = "1")
public class UserController {

    private final UserService userService;
    private final ApplicationContext context;
    private final Environment environment; // Proporciona acceso a propiedades y variables de entorno

    @GetMapping(path = "/info")
    public ResponseEntity<Object> getInfo() {
        Map<String, Object> body = Map.of(
                "users", this.userService.findAllUsers(),
                "POD_NAME", Objects.requireNonNull(this.environment.getProperty("MY_POD_NAME")),
                "POD_IP", Objects.requireNonNull(this.environment.getProperty("MY_POD_IP")),
                "config_text", Objects.requireNonNull(this.environment.getProperty("config.text"))
        );
        return ResponseEntity.ok(body);
    }

    @GetMapping(path = "/simulate-error")
    public void simulateError() {
        var configurableApplicationContext = (ConfigurableApplicationContext) this.context;
        configurableApplicationContext.close();
    }

    @GetMapping
    public ResponseEntity<List<UserResponse>> findAllUsers() {
        log.info("Solicitud recibida para obtener todos los usuarios");
        return ResponseEntity.ok(this.userService.findAllUsers());
    }

    @GetMapping(path = "/{userId}")
    public ResponseEntity<UserResponse> findUser(@PathVariable Long userId) {
        log.info("Solicitud recibida para obtener usuario con id: {}", userId);
        return ResponseEntity.ok(this.userService.findUser(userId));
    }

    @PostMapping
    public ResponseEntity<UserResponse> saveUser(@Valid @RequestBody UserRequest userRequest) {
        UserResponse userResponse = this.userService.saveUser(userRequest);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{userId}")
                .buildAndExpand(userResponse.id())
                .toUri();
        return ResponseEntity.created(location).body(userResponse);
    }

    @PutMapping(path = "/{userId}")
    public ResponseEntity<UserResponse> updateUser(@PathVariable Long userId, @Valid @RequestBody UserRequest userRequest) {
        return ResponseEntity.ok(this.userService.updateUser(userId, userRequest));
    }

    @DeleteMapping(path = "/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long userId) {
        this.userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }

    //-- 🌐 Para la comunicación desde el course-service ---

    /**
     * Endpoint para la recuperación masiva de usuarios.
     *
     * @param userIds Lista de IDs enviados como parámetros en la URL (ej: ?userIds=1,2,3).
     */
    @GetMapping(path = "/by-ids")
    public ResponseEntity<List<UserResponse>> findUsersByUserIds(@RequestParam List<Long> userIds) {
        return ResponseEntity.ok(this.userService.findUsersByIds(userIds));
    }
}
