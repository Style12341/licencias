package met.agiles.licencias.controllers;

import met.agiles.licencias.configuration.SecurityConfig;
import met.agiles.licencias.enums.PaymentMethod;
import met.agiles.licencias.persistance.models.Holder;
import met.agiles.licencias.persistance.models.License;
import met.agiles.licencias.persistance.repository.HolderRepository;
import met.agiles.licencias.persistance.repository.UsuarioRepository;
import met.agiles.licencias.services.CustomUserDetailsService;
import met.agiles.licencias.services.LicenseService;
import met.agiles.licencias.services.PdfGeneratorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdministrativoController.class)
@Import(SecurityConfig.class)
class AdministrativoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    // Mockear los servicios que son dependencias del controlador
    @MockBean
    private LicenseService licenseService;

    @MockBean
    private HolderRepository holderRepository;

    @MockBean
    private UsuarioRepository usuarioRepository;

    @MockBean
    private PdfGeneratorService pdfGeneratorService;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    private License mockLicense;
    private Holder mockHolder;


    @BeforeEach
    void setUp() {
        mockHolder = new Holder();
        mockHolder.setDni("12345678");
        mockHolder.setName("JUAN");
        mockHolder.setLastName("GONZALEZ");
        mockHolder.setBirthDate(LocalDate.of(1990, 5, 15));

        mockLicense = new License();
        mockLicense.setId(1L);
        mockLicense.setLast_name("GONZALEZ");
        mockLicense.setFirst_name("JUAN");
        mockLicense.setDni("12345678");
        mockLicense.setBirthDate(LocalDate.of(1990, 5, 15));
        mockLicense.setIssuanceDate(LocalDate.now());
        mockLicense.setExpirationDate(LocalDate.now().plusYears(5));
        mockLicense.setHolder(mockHolder);
    }

    @Test
    @WithMockUser(roles = "ADMINISTRATIVO")
    void mostrarLicenciasConFiltro_deberiaRetornarVistaConLicencias() throws Exception {
        // Simular que el servicio devuelve una lista con nuestra licencia de prueba
        when(licenseService.searchFilteredLicenses(any(), any(), any())).thenReturn(List.of(mockLicense));

        mockMvc.perform(get("/administrativo/licencias/list")
                        .param("dni", "12345678")
                        .param("orden", "desc"))
                .andExpect(status().isOk())
                .andExpect(view().name("administrativo/licensesList"))
                .andExpect(model().attributeExists("licencias"))
                .andExpect(model().attribute("licencias", List.of(mockLicense)));

        // Verificar que el servicio fue llamado con los parámetros correctos
        verify(licenseService).searchFilteredLicenses("12345678", null, "desc");
    }

    @Test
    @WithMockUser(roles = "ADMINISTRATIVO")
    void showAndSearchLicensesPage_cuandoNoHayBusqueda_deberiaRetornarVistaVacia() throws Exception {
        mockMvc.perform(get("/administrativo/licencias/buscar"))
                .andExpect(status().isOk())
                .andExpect(view().name("administrativo/searchLicenses"))
                .andExpect(model().attribute("licencias", Collections.emptyList()))
                .andExpect(model().attribute("busquedaRealizada", false));

        // Verificar que el servicio NUNCA fue llamado porque no hay parámetros de búsqueda
        verify(licenseService, never()).searchFilteredLicenses(any(), any(), any());
    }

    @Test
    @WithMockUser(roles = "ADMINISTRATIVO")
    void showAndSearchLicensesPage_cuandoHayBusqueda_deberiaRetornarResultados() throws Exception {
        // Simular que el servicio encuentra una licencia
        when(licenseService.searchFilteredLicenses("12345678", null, "asc")).thenReturn(List.of(mockLicense));

        mockMvc.perform(get("/administrativo/licencias/buscar")
                        .param("dni", "12345678"))
                .andExpect(status().isOk())
                .andExpect(view().name("administrativo/searchLicenses"))
                .andExpect(model().attribute("licencias", List.of(mockLicense)))
                .andExpect(model().attribute("busquedaRealizada", true));

        // Verificar que el servicio fue llamado
        verify(licenseService).searchFilteredLicenses("12345678", null, "asc");
    }

    @Test
    @WithMockUser(roles = "ADMINISTRATIVO")
    void showPrintLicensePage_cuandoLicenciaExiste_deberiaRetornarVistaImpresion() throws Exception {
        // Simular que el servicio encuentra la licencia por ID
        when(licenseService.getLicenseById(1L)).thenReturn(mockLicense);

        mockMvc.perform(get("/administrativo/licencias/imprimir/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("administrativo/licenseToPrint"))
                .andExpect(model().attributeExists("licencia"))
                .andExpect(model().attribute("licencia", mockLicense));
    }

    @Test
    @WithMockUser(roles = "ADMINISTRATIVO")
    void showPrintLicensePage_cuandoLicenciaNoExiste_deberiaRedirigirConError() throws Exception {
        // Simular que el servicio NO encuentra la licencia
        when(licenseService.getLicenseById(anyLong())).thenReturn(null);

        mockMvc.perform(get("/administrativo/licencias/imprimir/99"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/administrativo/licencias/buscar?error=notfound"));
    }

    @Test
    @WithMockUser(roles = "ADMINISTRATIVO")
    void generarLicenciaPdf_cuandoLicenciaExiste_deberiaRetornarPdf() throws Exception {
        byte[] pdfBytes = "Este es un PDF falso".getBytes(); // Contenido del PDF simulado

        when(licenseService.getLicenseById(1L)).thenReturn(mockLicense);
        when(pdfGeneratorService.generateLicensePdf(mockLicense)).thenReturn(pdfBytes);

        mockMvc.perform(get("/administrativo/licencias/generar-pdf/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                .andExpect(header().string("Content-Disposition", "form-data; name=\"attachment\"; filename=\"licencia_1_GONZALEZ.pdf\""))
                .andExpect(content().bytes(pdfBytes));

        // Verificar que los servicios fueron llamados como se esperaba
        verify(licenseService, times(1)).getLicenseById(1L);
        verify(pdfGeneratorService, times(1)).generateLicensePdf(mockLicense);
    }

    @Test
    @WithMockUser(roles = "ADMINISTRATIVO")
    void generarLicenciaPdf_cuandoLicenciaNoExiste_deberiaRetornarNotFound() throws Exception {
        // Simular que la licencia no se encuentra
        when(licenseService.getLicenseById(99L)).thenReturn(null);

        mockMvc.perform(get("/administrativo/licencias/generar-pdf/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMINISTRATIVO")
    void generarLicenciaPdf_cuandoHayErrorDeGeneracion_deberiaRetornarError500() throws Exception {
        // Simular que la generación del PDF lanza una excepción
        when(licenseService.getLicenseById(1L)).thenReturn(mockLicense);
        when(pdfGeneratorService.generateLicensePdf(mockLicense)).thenThrow(new RuntimeException("Error al crear PDF"));

        mockMvc.perform(get("/administrativo/licencias/generar-pdf/1"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @WithMockUser(roles = "ADMINISTRATIVO")
    void savePaymentMethod_cuandoEsExitoso_deberiaRetornarOk() throws Exception {
        when(licenseService.assignPaymentToLicense(1L, PaymentMethod.TARJETA_DE_CREDITO))
                .thenReturn(mockLicense);

        mockMvc.perform(post("/administrativo/licencias/guardar-metodo-pago/1")
                        .param("paymentMethod", "TARJETA_DE_CREDITO"))
                .andExpect(status().isOk())
                .andExpect(content().string("Método de pago guardado exitosamente"));

        // Verifica que el método del servicio fue llamado con los argumentos correctos
        verify(licenseService).assignPaymentToLicense(1L, PaymentMethod.TARJETA_DE_CREDITO);
    }

    @Test
    @WithMockUser(roles = "ADMINISTRATIVO")
    void savePaymentMethod_cuandoFalla_deberiaRetornarErrorInterno() throws Exception {
        // Simular que el servicio lanza una excepción
        doThrow(new RuntimeException("Error en base de datos")).when(licenseService).assignPaymentToLicense(anyLong(), any(PaymentMethod.class));

        mockMvc.perform(post("/administrativo/licencias/guardar-metodo-pago/1")
                        .param("paymentMethod", "EFECTIVO"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Error al guardar el método de pago"));
    }

}
