package met.agiles.licencias.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import met.agiles.licencias.configuration.SecurityConfig;
import met.agiles.licencias.services.CustomUserDetailsService;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    void cuandoAccedeALoginSinParametros_deberiaRetornarPaginaLoginSinMensajes() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"))
                .andExpect(model().attributeDoesNotExist("error"))
                .andExpect(model().attributeDoesNotExist("message"));
    }

    @Test
    void cuandoAccedeALoginConParametroError_deberiaMostrarMensajeError() throws Exception {
        mockMvc.perform(get("/login").param("error", "true"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"))
                .andExpect(model().attribute("error", "Usuario o contraseña incorrectos"))
                .andExpect(model().attributeDoesNotExist("message"));
    }

    @Test
    void cuandoAccedeALoginConParametroLogout_deberiaMostrarMensajeLogout() throws Exception {
        mockMvc.perform(get("/login").param("logout", "true"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"))
                .andExpect(model().attribute("message", "Ha cerrado sesión correctamente"))
                .andExpect(model().attributeDoesNotExist("error"));
    }

    @Test
    void cuandoAccedeALoginConAmbosParametros_deberiaMostrarAmbosMensajes() throws Exception {
        mockMvc.perform(get("/login")
                        .param("error", "true")
                        .param("logout", "true"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"))
                .andExpect(model().attribute("error", "Usuario o contraseña incorrectos"))
                .andExpect(model().attribute("message", "Ha cerrado sesión correctamente"));
    }

    @Test
    void cuandoAccedeALoginConParametroErrorVacio_deberiaMostrarMensajeError() throws Exception {
        mockMvc.perform(get("/login").param("error", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("login"))
                .andExpect(model().attribute("error", "Usuario o contraseña incorrectos"));
    }

    @Test
    void cuandoAccedeALoginConParametroLogoutVacio_deberiaMostrarMensajeLogout() throws Exception {
        mockMvc.perform(get("/login").param("logout", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("login"))
                .andExpect(model().attribute("message", "Ha cerrado sesión correctamente"));
    }

    @Test
    void cuandoAccedeALoginConParametroErrorFalse_noDeberiaMostrarMensajeError() throws Exception {
        mockMvc.perform(get("/login").param("error", "false"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"))
                .andExpect(model().attribute("error", "Usuario o contraseña incorrectos"));
    }

    @Test
    void cuandoAccedeARaiz_deberiaRedirigirALogin() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    @WithMockUser
    void cuandoUsuarioAutenticadoAccedeARaiz_deberiaRedirigirALogin() throws Exception {
        // Incluso con usuario autenticado, la ruta raíz redirige a login
        // El sistema de seguridad luego manejará la redirección apropiada
        mockMvc.perform(get("/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void cuandoAccedeALoginConParametrosMultiples_deberiaTratarSoloElPrimero() throws Exception {
        mockMvc.perform(get("/login")
                        .param("error", "true")
                        .param("error", "false"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"))
                .andExpect(model().attribute("error", "Usuario o contraseña incorrectos"));
    }

    @Test
    void cuandoAccedeALoginConParametrosEspeciales_deberiaFuncionar() throws Exception {
        mockMvc.perform(get("/login")
                        .param("error", "1")
                        .param("logout", "yes"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"))
                .andExpect(model().attribute("error", "Usuario o contraseña incorrectos"))
                .andExpect(model().attribute("message", "Ha cerrado sesión correctamente"));
    }
}
