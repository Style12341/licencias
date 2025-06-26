package met.agiles.licencias.services;

import met.agiles.licencias.persistance.models.User;
import met.agiles.licencias.persistance.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User createUser(User user, String adminUsername) {

        if (user.getUsername() == null || user.getUsername().isBlank()) {
            throw new IllegalArgumentException("El nombre de usuario es obligatorio");
        }

        if (user.getFirstName() == null || user.getFirstName().isBlank()) {
            throw new IllegalArgumentException("Por favor, ingrese el nombre");
        }

        if (user.getLastName() == null || user.getLastName().isBlank()) {
            throw new IllegalArgumentException("Por favor, ingrese el apellido");
        }

        if (user.getPassword() == null || user.getPassword().length() < 6) {
            throw new IllegalArgumentException("La contraseña debe tener al menos 6 caracteres");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        if (usuarioRepository.existsByUsername(user.getUsername())) {
            throw new IllegalArgumentException("Ya existe un usuario con ese nombre de usuario");
        }

        if(user.getRole() == null) {
            throw new IllegalArgumentException("Por favor, seleccione un rol");
        }

        User admin = usuarioRepository.findByUsername(adminUsername)
                    .orElseThrow(() -> new RuntimeException("Administrador no encontrado"));

        user.setCreatedByUser(admin);
        user.setCreationDate(java.time.LocalDateTime.now());

        return usuarioRepository.save(user);
    }

    public List<User> getAllUsers() {
        return usuarioRepository.findAll();
    }
}
