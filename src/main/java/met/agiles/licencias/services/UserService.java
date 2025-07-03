package met.agiles.licencias.services;

import jakarta.persistence.EntityNotFoundException;
import met.agiles.licencias.persistance.models.User;
import met.agiles.licencias.persistance.models.UserModificationAudit;
import met.agiles.licencias.persistance.repository.UserModificationAuditRepository;
import met.agiles.licencias.persistance.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserModificationAuditRepository userModificationAuditRepository;

    public UserService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder, UserModificationAuditRepository userModificationAuditRepository) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.userModificationAuditRepository = userModificationAuditRepository;
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

    public User actualizarDatosUsuario(User userForm, String adminUsername) {
        User existing = usuarioRepository.findById(userForm.getId())
                .orElseThrow(() -> new IllegalArgumentException("Usuario no existe"));

        // Actualizo los campos editables
        existing.setFirstName(userForm.getFirstName());
        existing.setLastName(userForm.getLastName());
        existing.setProvincia(userForm.getProvincia());
        existing.setCiudad(userForm.getCiudad());

        User saved = usuarioRepository.save(existing);

        User performedBy = usuarioRepository.findByUsername(adminUsername)
                .orElseThrow(() -> new IllegalArgumentException("Administrativo no existe"));

        UserModificationAudit audit = UserModificationAudit.builder()
                .targetUser(saved)
                .performedBy(performedBy)
                .performedAt(LocalDateTime.now())
                .build();

        userModificationAuditRepository.save(audit);

        return saved;
    }

    public List<User> buscarPorNombre(String nombre) {

        return usuarioRepository.findByFirstNameContainingIgnoreCaseAndActiveTrue(nombre);

    }

    public void disableUser(Long id) {
            User u = usuarioRepository.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));
            u.setActive(false);
            usuarioRepository.save(u);
        }

    public User getUserById(Long id) {return usuarioRepository.findById(id).orElse(null);}
    public List<User> getAllUsers() {
        return usuarioRepository.findByActiveTrue();
    }
}
