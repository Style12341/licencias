package met.agiles.licencias.service;

import met.agiles.licencias.enums.LicenseClass;
import met.agiles.licencias.persistance.models.Holder;
import met.agiles.licencias.persistance.models.License;
import met.agiles.licencias.persistance.models.LicensePricing;
import met.agiles.licencias.persistance.repository.LicensePricingRepository;
import met.agiles.licencias.persistance.repository.LicenseRepository;
import met.agiles.licencias.services.LicenseService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LicenseServiceTest {

    private LicensePricingRepository pricingRepository;
    private LicenseRepository licenseRepository;
    private LicenseService licenseService;

    @BeforeEach
    void setUp() {
        pricingRepository = mock(LicensePricingRepository.class);
        licenseRepository = mock(LicenseRepository.class);
        licenseService = new LicenseService(pricingRepository, licenseRepository);

    }

    @Test
    void calcularCostoTotal_retornaCostoCorrecto() {
        // Arrange
        License license = new License();
        license.setLicenseClasses(List.of(LicenseClass.A, LicenseClass.B));
        license.setIssuanceDate(LocalDate.of(2024, 6, 1));
        license.setExpirationDate(LocalDate.of(2027, 6, 1)); // vigencia de 3 años

        LicensePricing tarifaA = new LicensePricing();
        tarifaA.setPrice(25);
        LicensePricing tarifaB = new LicensePricing();
        tarifaB.setPrice(25);

        when(pricingRepository.findByLicenseClassAndValidityYears(LicenseClass.A, 3))
                .thenReturn(tarifaA);
        when(pricingRepository.findByLicenseClassAndValidityYears(LicenseClass.B, 3))
                .thenReturn(tarifaB);

        // Act
        double resultado = licenseService.calcularCostoTotal(license);

        // Assert
        assertEquals(25 + 25 + LicensePricing.getBasePrice(), resultado);
    }

    @Test
    void calcularCostoTotal_lanzaExcepcionSiNoEncuentraTarifa() {
        // Arrange
        License license = new License();
        license.setLicenseClasses(List.of(LicenseClass.C));
        license.setIssuanceDate(LocalDate.of(2024, 6, 1));
        license.setExpirationDate(LocalDate.of(2029, 6, 1)); // vigencia de 5 años

        when(pricingRepository.findByLicenseClassAndValidityYears(LicenseClass.C, 5))
                .thenReturn(null); //no se encuentra tarifa

        // Act & Assert
        assertThrows(NullPointerException.class, () -> {
            licenseService.calcularCostoTotal(license);
        });
    }

    @Test
    public void testCalcularExpiracion_Menor21_SinLicenciaPrevia() {
        // Arrange
        LocalDate today = LocalDate.now();

        Holder holder = new Holder();
        holder.setDni("12345678");
        holder.setBirthDate(today.minusYears(18)); // 18 años

        License license = new License();
        license.setHolder(holder);

        when(licenseRepository.findByDni("12345678")).thenReturn(Collections.emptyList());

        // Act
        licenseService.calcularExpiracion(license);

        // Assert
        assertEquals(today.plusYears(1), license.getExpirationDate());
        assertEquals("Principiante por primeros 6 meses.", license.getObvservations());
    }

    @Test
    public void testCalcularExpiracion_Menor21_ConLicenciaPrevia() {
        LocalDate today = LocalDate.now();

        Holder holder = new Holder();
        holder.setDni("87654321");
        holder.setBirthDate(today.minusYears(19)); // 19 años

        License license = new License();
        license.setHolder(holder);

        when(licenseRepository.findByDni("87654321")).thenReturn(List.of(new License()));

        licenseService.calcularExpiracion(license);

        assertEquals(today.plusYears(3), license.getExpirationDate());
    }

    @Test
    public void testCalcularExpiracion_Entre21y45() {
        LocalDate today = LocalDate.now();

        Holder holder = new Holder();
        holder.setDni("22222222");
        holder.setBirthDate(today.minusYears(30)); // 30 años

        License license = new License();
        license.setHolder(holder);

        when(licenseRepository.findByDni("22222222")).thenReturn(List.of(new License()));

        licenseService.calcularExpiracion(license);

        assertEquals(today.plusYears(5), license.getExpirationDate());
    }

    @Test
    public void testCalcularExpiracion_Mayor70() {
        LocalDate today = LocalDate.now();

        Holder holder = new Holder();
        holder.setDni("99999999");
        holder.setBirthDate(today.minusYears(75)); // 75 años

        License license = new License();
        license.setHolder(holder);

        when(licenseRepository.findByDni("99999999")).thenReturn(List.of(new License()));

        licenseService.calcularExpiracion(license);

        assertEquals(today.plusYears(1), license.getExpirationDate());
    }

    @Test
    public void testCreateLicense_cuandoHolderTieneLicenciaActiva_deberiaInvalidarLicenciaAnterior() {
        // Arrange
        LocalDate today = LocalDate.now();
        
        // Holder con licencia activa previa
        Holder holder = new Holder();
        holder.setDni("12345678");
        holder.setBirthDate(today.minusYears(30));
        
        // Licencia activa existente
        License licenciaActiva = new License();
        licenciaActiva.setId(1L);
        licenciaActiva.setDni("12345678");
        licenciaActiva.setIsValid(true);
        licenciaActiva.setExpirationDate(today.plusYears(2));
        
        // Nueva licencia a crear
        License nuevaLicencia = new License();
        nuevaLicencia.setHolder(holder);
        nuevaLicencia.setDni("12345678");
        nuevaLicencia.setLicenseClasses(List.of(met.agiles.licencias.enums.LicenseClass.A));
        nuevaLicencia.setIssuanceDate(today);
        nuevaLicencia.setExpirationDate(today.plusYears(5));
        
        // Mock repository behavior
        when(licenseRepository.findByDniAndExpirationDateGreaterThanEqualAndIsValidTrue("12345678", today))
                .thenReturn(Optional.of(licenciaActiva));
        when(licenseRepository.save(any(License.class))).thenAnswer(invocation -> {
            License license = invocation.getArgument(0);
            if (license.getId() != null && license.getId().equals(1L)) {
                // Esta es la licencia activa que debe ser invalidada
                license.setIsValid(false);
            }
            return license;
        });
        
        // Mocks for pricing calculation
        LicensePricing tarifaA = new LicensePricing();
        tarifaA.setPrice(20);
        when(pricingRepository.findByLicenseClassAndValidityYears(
                met.agiles.licencias.enums.LicenseClass.A, 5))
                .thenReturn(tarifaA);
        
        // Act
        License result = licenseService.createLicense(nuevaLicencia);
        
        // Assert
        assertNotNull(result);
        
        // Verificar que se intentó encontrar la licencia activa
        verify(licenseRepository).findByDniAndExpirationDateGreaterThanEqualAndIsValidTrue("12345678", today);
        
        // Verificar que se guardó (invalidó) la licencia activa existente
        verify(licenseRepository, times(2)).save(any(License.class)); // Una vez para invalidar, otra para crear
    }
    
    @Test
    public void testCreateLicense_cuandoHolderNoTieneLicenciaActiva_noDeberiaInvalidarNinguna() {
        // Arrange
        LocalDate today = LocalDate.now();
        
        // Holder sin licencia activa previa
        Holder holder = new Holder();
        holder.setDni("87654321");
        holder.setBirthDate(today.minusYears(25));
        
        // Nueva licencia a crear
        License nuevaLicencia = new License();
        nuevaLicencia.setHolder(holder);
        nuevaLicencia.setDni("87654321");
        nuevaLicencia.setLicenseClasses(List.of(met.agiles.licencias.enums.LicenseClass.B));
        nuevaLicencia.setIssuanceDate(today);
        nuevaLicencia.setExpirationDate(today.plusYears(5));
        
        // Mock repository behavior - no hay licencia activa
        when(licenseRepository.findByDniAndExpirationDateGreaterThanEqualAndIsValidTrue("87654321", today))
                .thenReturn(Optional.empty());
        when(licenseRepository.save(any(License.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // Mocks for pricing calculation
        LicensePricing tarifaB = new LicensePricing();
        tarifaB.setPrice(20);
        when(pricingRepository.findByLicenseClassAndValidityYears(
                met.agiles.licencias.enums.LicenseClass.B, 5))
                .thenReturn(tarifaB);
        
        // Act
        License result = licenseService.createLicense(nuevaLicencia);
        
        // Assert
        assertNotNull(result);
        
        // Verificar que se intentó encontrar la licencia activa
        verify(licenseRepository).findByDniAndExpirationDateGreaterThanEqualAndIsValidTrue("87654321", today);
        
        // Verificar que se guardó solo la nueva licencia (no había ninguna para invalidar)
        verify(licenseRepository, times(1)).save(any(License.class)); // Solo para crear la nueva
    }
    
    @Test
    public void testInvalidateActiveLicense_cuandoExisteLicenciaActiva_deberiaInvalidarla() {
        // Arrange
        LocalDate today = LocalDate.now();
        
        Holder holder = new Holder();
        holder.setDni("11111111");
        
        License licenciaActiva = new License();
        licenciaActiva.setId(5L);
        licenciaActiva.setDni("11111111");
        licenciaActiva.setIsValid(true);
        licenciaActiva.setExpirationDate(today.plusYears(3));
        
        when(licenseRepository.findByDniAndExpirationDateGreaterThanEqualAndIsValidTrue("11111111", today))
                .thenReturn(Optional.of(licenciaActiva));
        when(licenseRepository.save(any(License.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // Act
        licenseService.invalidateActiveLicense(holder);
        
        // Assert
        verify(licenseRepository).findByDniAndExpirationDateGreaterThanEqualAndIsValidTrue("11111111", today);
        verify(licenseRepository).save(argThat(license -> 
            license.getId().equals(5L) && !license.getIsValid()
        ));
    }
    
    @Test
    public void testInvalidateActiveLicense_cuandoNoExisteLicenciaActiva_noDeberiaHacerNada() {
        // Arrange
        LocalDate today = LocalDate.now();
        
        Holder holder = new Holder();
        holder.setDni("22222222");
        
        when(licenseRepository.findByDniAndExpirationDateGreaterThanEqualAndIsValidTrue("22222222", today))
                .thenReturn(Optional.empty());
        
        // Act
        licenseService.invalidateActiveLicense(holder);
        
        // Assert
        verify(licenseRepository).findByDniAndExpirationDateGreaterThanEqualAndIsValidTrue("22222222", today);
        verify(licenseRepository, never()).save(any(License.class));
    }
    
    @Test
    public void testGetActiveLicenseByHolderDni_cuandoExisteLicenciaActiva_deberiaRetornarla() {
        // Arrange
        LocalDate today = LocalDate.now();
        String dni = "33333333";
        
        License licenciaActiva = new License();
        licenciaActiva.setId(6L);
        licenciaActiva.setDni(dni);
        licenciaActiva.setIsValid(true);
        licenciaActiva.setExpirationDate(today.plusYears(2));
        
        when(licenseRepository.findByDniAndExpirationDateGreaterThanEqualAndIsValidTrue(dni, today))
                .thenReturn(Optional.of(licenciaActiva));
        
        // Act
        Optional<License> result = licenseService.getActiveLicenseByHolderDni(dni);
        
        // Assert
        assertTrue(result.isPresent());
        assertEquals(6L, result.get().getId());
        assertEquals(dni, result.get().getDni());
        assertTrue(result.get().getIsValid());
    }
    
    @Test
    public void testGetActiveLicenseByHolderDni_cuandoNoExisteLicenciaActiva_deberiaRetornarEmpty() {
        // Arrange
        LocalDate today = LocalDate.now();
        String dni = "44444444";
        
        when(licenseRepository.findByDniAndExpirationDateGreaterThanEqualAndIsValidTrue(dni, today))
                .thenReturn(Optional.empty());
        
        // Act
        Optional<License> result = licenseService.getActiveLicenseByHolderDni(dni);
        
        // Assert
        assertFalse(result.isPresent());
    }
}