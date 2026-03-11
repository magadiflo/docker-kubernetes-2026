package dev.magadiflo.user.app.repository;

import dev.magadiflo.user.app.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    /**
     * Consulta derivada para verificar la existencia de un usuario por su email.
     *
     * @param email Correo a validar
     * @return true si el email ya está registrado
     */
    boolean existsByEmail(String email);
}
