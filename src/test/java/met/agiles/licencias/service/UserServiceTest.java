
package met.agiles.licencias.service;

import met.agiles.licencias.enums.Role;
import met.agiles.licencias.persistance.models.User;
import met.agiles.licencias.persistance.models.UserModificationAudit;
import met.agiles.licencias.persistance.repository.UserModificationAuditRepository;
import met.agiles.licencias.persistance.repository.UsuarioRepository;
import met.agiles.licencias.services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {

    private UsuarioRepository       userRepo;
    private PasswordEncoder         passwordEncoder;
    private UserModificationAuditRepository auditRepo;
    private UserService service;

    @BeforeEach
    void setUp() {
        userRepo        = mock(UsuarioRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);
        auditRepo       = mock(UserModificationAuditRepository.class);
        service         = new UserService(userRepo, passwordEncoder, auditRepo);
    }

    @Test
    void createUser_success() {
        // Arrange
        User dto = new User();
        dto.setUsername("pepito");
        dto.setPassword("secreto");
        dto.setFirstName("Pepe");
        dto.setLastName("Perez");
        dto.setRole(Role.ADMINISTRATIVO);

        when(passwordEncoder.encode("secreto")).thenReturn("hash");
        when(userRepo.existsByUsername("pepito")).thenReturn(false);

        User admin = new User();
        admin.setUsername("admin");
        when(userRepo.findByUsername("admin")).thenReturn(Optional.of(admin));

        // Act
        User saved = service.createUser(dto, "admin");

        // Assert
        assertEquals("hash", saved.getPassword());

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepo).save(captor.capture());
        User toSave = captor.getValue();

        assertEquals("pepito", toSave.getUsername());
        assertEquals("hash", toSave.getPassword());
        assertEquals(Role.ADMINISTRATIVO, toSave.getRole());
        assertNotNull(toSave.getCreatedByUser());
        assertEquals("admin", toSave.getCreatedByUser().getUsername());
        assertNotNull(toSave.getCreationDate());
    }

    @Test
    void createUser_shortPassword_throws() {
        User dto = new User();
        dto.setUsername("juan");
        dto.setPassword("123");
        dto.setFirstName("Juan");
        dto.setLastName("Gomez");
        dto.setRole(Role.ADMINISTRATIVO);

        Exception ex = assertThrows(IllegalArgumentException.class, () ->
                service.createUser(dto, "admin")
        );
        assertTrue(ex.getMessage().contains("contraseña"));
        verifyNoInteractions(userRepo);
    }

    @Test
    void actualizarDatosUsuario_success() {
        User existing = new User();
        existing.setId(42L);
        existing.setFirstName("Old");
        existing.setLastName("Name");
        existing.setProvincia("X");
        existing.setCiudad("Y");
        when(userRepo.findById(42L)).thenReturn(Optional.of(existing));

        User performedBy = new User();
        performedBy.setUsername("adminUser");
        when(userRepo.findByUsername("adminUser")).thenReturn(Optional.of(performedBy));

        User dto = new User();
        dto.setId(42L);
        dto.setFirstName("New");
        dto.setLastName("Name2");
        dto.setProvincia("Pcia");
        dto.setCiudad("Cdad");

        // Act
        User updated = service.actualizarDatosUsuario(dto, "adminUser");

        // Assert saved fields
        ArgumentCaptor<User> userCap = ArgumentCaptor.forClass(User.class);
        verify(userRepo).save(userCap.capture());
        User saved = userCap.getValue();
        assertEquals("New",   saved.getFirstName());
        assertEquals("Name2", saved.getLastName());
        assertEquals("Pcia",  saved.getProvincia());
        assertEquals("Cdad",  saved.getCiudad());

        // Assert audit
        ArgumentCaptor<UserModificationAudit> auditCap =
                ArgumentCaptor.forClass(UserModificationAudit.class);
        verify(auditRepo).save(auditCap.capture());
        UserModificationAudit audit = auditCap.getValue();
        assertEquals(saved,       audit.getTargetUser());
        assertEquals(performedBy, audit.getPerformedBy());
        assertNotNull(audit.getPerformedAt());
    }

    @Test
    void actualizarDatosUsuario_nonexistent_throws() {
        when(userRepo.findById(99L)).thenReturn(Optional.empty());
        User dto = new User();
        dto.setId(99L);

        Exception ex = assertThrows(IllegalArgumentException.class, () ->
                service.actualizarDatosUsuario(dto, "adminUser")
        );
        assertTrue(ex.getMessage().contains("no existe"));
        verifyNoMoreInteractions(auditRepo);
    }
}
