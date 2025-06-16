package met.agiles.licencias.integration;

import met.agiles.licencias.enums.Role;
import met.agiles.licencias.persistance.models.User;
import met.agiles.licencias.persistance.repository.UsuarioRepository;
import met.agiles.licencias.persistance.repository.LicenseRepository;
import met.agiles.licencias.persistance.repository.PaymentReceiptRepository;
import met.agiles.licencias.persistance.repository.HolderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class SecurityIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();
        
        // Verificar que no existan usuarios antes de crear
        if (usuarioRepository.findByUsername("testadmin").isEmpty()) {
            User adminUser = new User();
            adminUser.setUsername("testadmin");
            adminUser.setPassword(passwordEncoder.encode("admin123"));
            adminUser.setRole(Role.ADMINISTRADOR);
            adminUser.setProvincia("Buenos Aires");
            adminUser.setCiudad("CABA");
            usuarioRepository.save(adminUser);
        }

        if (usuarioRepository.findByUsername("testadministrativo").isEmpty()) {
            User administrativoUser = new User();
            administrativoUser.setUsername("testadministrativo");
            administrativoUser.setPassword(passwordEncoder.encode("admin123"));
            administrativoUser.setRole(Role.ADMINISTRATIVO);
            administrativoUser.setProvincia("Córdoba");
            administrativoUser.setCiudad("Córdoba Capital");
            usuarioRepository.save(administrativoUser);
        }
    }

    @Test
    void cuandoAccedeALoginPageSinError_deberiaNoMostrarMensajeError() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"))
                .andExpect(model().attributeDoesNotExist("error"))
                .andExpect(model().attributeDoesNotExist("message"));
    }

    @Test
    void cuandoAccedeALoginPageConError_deberiaMostrarMensajeError() throws Exception {
        mockMvc.perform(get("/login").param("error", "true"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"))
                .andExpect(model().attribute("error", "Usuario o contraseña incorrectos"));
    }

    @Test
    void cuandoAccedeALoginPageDespuesDeLogout_deberiaMostrarMensajeLogout() throws Exception {
        mockMvc.perform(get("/login").param("logout", "true"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"))
                .andExpect(model().attribute("message", "Ha cerrado sesión correctamente"));
    }

    @Test
    void cuandoAccedeARaiz_deberiaRedirigirALogin() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    @WithMockUser(username = "testadmin", roles = {"ADMINISTRADOR"})
    void cuandoAdministradorAccedeAAdminHome_deberiaPermitirAcceso() throws Exception {
        mockMvc.perform(get("/admin/home"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/home"))
                .andExpect(model().attributeExists("title"))
                .andExpect(model().attributeExists("usuario"));
    }

    @Test
    @WithMockUser(username = "testadministrativo", roles = {"ADMINISTRATIVO"})
    void cuandoAdministrativoAccedeAAdministrativoHome_deberiaPermitirAcceso() throws Exception {
        mockMvc.perform(get("/administrativo/home"))
                .andExpect(status().isOk())
                .andExpect(view().name("administrativo/home"));
    }

    @Test
    @WithMockUser(username = "testadministrativo", roles = {"ADMINISTRATIVO"})
    void cuandoAdministrativoAccedeAAdminHome_deberiaDenegarAcceso() throws Exception {
        mockMvc.perform(get("/admin/home"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "testadmin", roles = {"ADMINISTRADOR"})
    void cuandoAdministradorAccedeAAdministrativoHome_deberiaDenegarAcceso() throws Exception {
        mockMvc.perform(get("/administrativo/home"))
                .andExpect(status().isForbidden());
    }

    @Test
    void cuandoUsuarioNoAutenticadoAccedeARecursoProtegido_deberiaRedirigirALogin() throws Exception {
        mockMvc.perform(get("/admin/home"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    void cuandoUsuarioNoAutenticadoAccedeAAdministrativo_deberiaRedirigirALogin() throws Exception {
        mockMvc.perform(get("/administrativo/home"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    void cuandoAccedeARecursosEstaticos_deberiaPermitirAccesoSinAutenticacion() throws Exception {
        // Los recursos estáticos deberían estar accesibles sin autenticación
        // Aunque estos archivos no existan en el proyecto de prueba, la configuración debería permitirlos
        mockMvc.perform(get("/css/style.css"))
                .andExpect(status().isNotFound()); // 404 porque el archivo no existe, no 403 Forbidden

        mockMvc.perform(get("/js/script.js"))
                .andExpect(status().isNotFound()); // 404 porque el archivo no existe, no 403 Forbidden

        mockMvc.perform(get("/images/logo.png"))
                .andExpect(status().isNotFound()); // 404 porque el archivo no existe, no 403 Forbidden
    }
}
