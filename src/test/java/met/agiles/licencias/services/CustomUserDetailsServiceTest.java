package met.agiles.licencias.services;

import met.agiles.licencias.enums.Role;
import met.agiles.licencias.persistance.models.User;
import met.agiles.licencias.persistance.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    private User adminUser;
    private User administrativoUser;

    @BeforeEach
    void setUp() {
        adminUser = new User();
        adminUser.setId(1L);
        adminUser.setUsername("admin");
        adminUser.setPassword("encodedPassword123");
        adminUser.setRole(Role.ADMINISTRADOR);
        adminUser.setProvincia("Buenos Aires");
        adminUser.setCiudad("CABA");

        administrativoUser = new User();
        administrativoUser.setId(2L);
        administrativoUser.setUsername("administrativo");
        administrativoUser.setPassword("encodedPassword456");
        administrativoUser.setRole(Role.ADMINISTRATIVO);
        administrativoUser.setProvincia("Córdoba");
        administrativoUser.setCiudad("Córdoba Capital");
    }

    @Test
    void cuandoLoadUserByUsernameConUsuarioAdministrador_deberiaRetornarUserDetailsCorrectamente() {
        // Arrange
        when(usuarioRepository.findByUsername("admin")).thenReturn(Optional.of(adminUser));

        // Act
        UserDetails result = customUserDetailsService.loadUserByUsername("admin");

        // Assert
        assertNotNull(result);
        assertEquals("admin", result.getUsername());
        assertEquals("encodedPassword123", result.getPassword());
        assertEquals(1, result.getAuthorities().size());
        
        GrantedAuthority authority = result.getAuthorities().iterator().next();
        assertEquals("ROLE_ADMINISTRADOR", authority.getAuthority());
        
        assertTrue(result.isEnabled());
        assertTrue(result.isAccountNonExpired());
        assertTrue(result.isAccountNonLocked());
        assertTrue(result.isCredentialsNonExpired());
        
        verify(usuarioRepository, times(1)).findByUsername("admin");
    }

    @Test
    void cuandoLoadUserByUsernameConUsuarioAdministrativo_deberiaRetornarUserDetailsCorrectamente() {
        // Arrange
        when(usuarioRepository.findByUsername("administrativo")).thenReturn(Optional.of(administrativoUser));

        // Act
        UserDetails result = customUserDetailsService.loadUserByUsername("administrativo");

        // Assert
        assertNotNull(result);
        assertEquals("administrativo", result.getUsername());
        assertEquals("encodedPassword456", result.getPassword());
        assertEquals(1, result.getAuthorities().size());
        
        GrantedAuthority authority = result.getAuthorities().iterator().next();
        assertEquals("ROLE_ADMINISTRATIVO", authority.getAuthority());
        
        assertTrue(result.isEnabled());
        assertTrue(result.isAccountNonExpired());
        assertTrue(result.isAccountNonLocked());
        assertTrue(result.isCredentialsNonExpired());
        
        verify(usuarioRepository, times(1)).findByUsername("administrativo");
    }

    @Test
    void cuandoLoadUserByUsernameConUsuarioNoExistente_deberiaLanzarUsernameNotFoundException() {
        // Arrange
        String nonExistentUsername = "noexiste";
        when(usuarioRepository.findByUsername(nonExistentUsername)).thenReturn(Optional.empty());

        // Act & Assert
        UsernameNotFoundException exception = assertThrows(
            UsernameNotFoundException.class,
            () -> customUserDetailsService.loadUserByUsername(nonExistentUsername)
        );
        
        assertEquals("Usuario no encontrado: " + nonExistentUsername, exception.getMessage());
        verify(usuarioRepository, times(1)).findByUsername(nonExistentUsername);
    }

    @Test
    void cuandoLoadUserByUsernameConUsernameNull_deberiaLanzarUsernameNotFoundException() {
        // Arrange
        when(usuarioRepository.findByUsername(null)).thenReturn(Optional.empty());

        // Act & Assert
        UsernameNotFoundException exception = assertThrows(
            UsernameNotFoundException.class,
            () -> customUserDetailsService.loadUserByUsername(null)
        );
        
        assertEquals("Usuario no encontrado: null", exception.getMessage());
        verify(usuarioRepository, times(1)).findByUsername(null);
    }

    @Test
    void cuandoLoadUserByUsernameConUsernameVacio_deberiaLanzarUsernameNotFoundException() {
        // Arrange
        String emptyUsername = "";
        when(usuarioRepository.findByUsername(emptyUsername)).thenReturn(Optional.empty());

        // Act & Assert
        UsernameNotFoundException exception = assertThrows(
            UsernameNotFoundException.class,
            () -> customUserDetailsService.loadUserByUsername(emptyUsername)
        );
        
        assertEquals("Usuario no encontrado: " + emptyUsername, exception.getMessage());
        verify(usuarioRepository, times(1)).findByUsername(emptyUsername);
    }

    @Test
    void cuandoLoadUserByUsernameConUsuarioSinRole_deberiaRetornarUserDetailsConRoleVacio() {
        // Arrange
        User userSinRole = new User();
        userSinRole.setId(3L);
        userSinRole.setUsername("usersinrole");
        userSinRole.setPassword("password");
        userSinRole.setRole(null);
        
        when(usuarioRepository.findByUsername("usersinrole")).thenReturn(Optional.of(userSinRole));

        // Act & Assert
        assertThrows(
            NullPointerException.class,
            () -> customUserDetailsService.loadUserByUsername("usersinrole")
        );
        
        verify(usuarioRepository, times(1)).findByUsername("usersinrole");
    }

    @Test
    void cuandoLoadUserByUsernameConCaseDiferente_deberiaSerCaseSensitive() {
        // Arrange
        when(usuarioRepository.findByUsername("ADMIN")).thenReturn(Optional.empty());

        // Act & Assert
        UsernameNotFoundException exception = assertThrows(
            UsernameNotFoundException.class,
            () -> customUserDetailsService.loadUserByUsername("ADMIN")
        );
        
        assertEquals("Usuario no encontrado: ADMIN", exception.getMessage());
        verify(usuarioRepository, times(1)).findByUsername("ADMIN");
        verify(usuarioRepository, never()).findByUsername("admin");
    }

    @Test
    void cuandoLoadUserByUsernameConEspacios_deberiaRespetarEspacios() {
        // Arrange
        String usernameConEspacios = " admin ";
        when(usuarioRepository.findByUsername(usernameConEspacios)).thenReturn(Optional.empty());

        // Act & Assert
        UsernameNotFoundException exception = assertThrows(
            UsernameNotFoundException.class,
            () -> customUserDetailsService.loadUserByUsername(usernameConEspacios)
        );
        
        assertEquals("Usuario no encontrado: " + usernameConEspacios, exception.getMessage());
        verify(usuarioRepository, times(1)).findByUsername(usernameConEspacios);
    }
}
