package dev.magadiflo.user.app.service.impl;

import dev.magadiflo.user.app.client.CourseServiceClient;
import dev.magadiflo.user.app.dto.UserRequest;
import dev.magadiflo.user.app.dto.UserResponse;
import dev.magadiflo.user.app.entity.User;
import dev.magadiflo.user.app.exception.EmailAlreadyExistsException;
import dev.magadiflo.user.app.exception.UserNotFoundException;
import dev.magadiflo.user.app.mapper.UserMapper;
import dev.magadiflo.user.app.repository.UserRepository;
import dev.magadiflo.user.app.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final CourseServiceClient courseServiceClient;
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public List<UserResponse> findAllUsers() {
        return this.userRepository.findAll()
                .stream()
                .map(this.userMapper::toUserResponse)
                .toList();
    }

    @Override
    public UserResponse findUser(Long userId) {
        return this.userRepository.findById(userId)
                .map(this.userMapper::toUserResponse)
                .orElseThrow(() -> new UserNotFoundException(userId));
    }

    @Override
    @Transactional
    public UserResponse saveUser(UserRequest userRequest) {
        if (this.userRepository.existsByEmail(userRequest.email())) {
            throw new EmailAlreadyExistsException(userRequest.email());
        }

        User savedUser = this.userRepository.save(this.userMapper.toUser(userRequest));
        return this.userMapper.toUserResponse(savedUser);
    }

    @Override
    @Transactional
    public UserResponse updateUser(Long userId, UserRequest userRequest) {
        return this.userRepository.findById(userId)
                .map(foundUser -> {
                    // 📧 Validación: Si el email cambia, verificamos que no exista ya
                    if (!userRequest.email().equalsIgnoreCase(foundUser.getEmail()) &&
                        this.userRepository.existsByEmail(userRequest.email())) {
                        throw new EmailAlreadyExistsException(userRequest.email());
                    }
                    return this.userMapper.toUpdateUser(foundUser, userRequest);
                })
                .map(this.userRepository::save)
                .map(this.userMapper::toUserResponse)
                .orElseThrow(() -> new UserNotFoundException(userId));
    }

    @Override
    @Transactional
    public void deleteUser(Long userId) {
        // 1. Verificación de existencia
        User foundUser = this.userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        // 2. Persistencia local
        this.userRepository.delete(foundUser);

        // 3. Orquestación distribuida (Limpieza de cursos)
        this.courseServiceClient.unassignUserFromAssociatedCourse(userId);
    }

    //-- 🌐 Para la comunicación desde el course-service ---

    /**
     * Recupera un listado de usuarios a partir de una colección de identificadores.
     * <p>
     * Se utiliza findAllById para ejecutar una única consulta SQL optimizada.
     *
     * @param userIds Lista de identificadores únicos.
     * @return Lista de UserResponse con la información detallada de cada usuario encontrado.
     */
    @Override
    public List<UserResponse> findUsersByIds(List<Long> userIds) {
        return this.userRepository.findAllById(userIds)
                .stream()
                .map(this.userMapper::toUserResponse)
                .toList();
    }
}
