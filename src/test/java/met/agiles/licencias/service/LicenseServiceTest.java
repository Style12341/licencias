package met.agiles.licencias.service;

import met.agiles.licencias.enums.LicenseClass;
import met.agiles.licencias.persistance.models.Holder;
import met.agiles.licencias.persistance.models.License;
import met.agiles.licencias.persistance.models.LicensePricing;
import met.agiles.licencias.persistance.models.User;
import met.agiles.licencias.persistance.repository.LicensePricingRepository;
import met.agiles.licencias.persistance.repository.LicenseRepository;
import met.agiles.licencias.services.LicenseService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
                .thenReturn(null); // no se encuentra tarifa

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
        licenseService.setExpiracionLicencia(license);

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

        licenseService.setExpiracionLicencia(license);

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

        licenseService.setExpiracionLicencia(license);

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

        licenseService.setExpiracionLicencia(license);

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

        // Verificar que se guardó solo la nueva licencia (no había ninguna para
        // invalidar)
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
        verify(licenseRepository).save(argThat(license -> license.getId().equals(5L) && !license.getIsValid()));
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

    @Test
    public void testMakeLicenseCopy_deberiaCrearCopiaConVersionIncrementada() {
        // Arrange
        LocalDate today = LocalDate.now();

        Holder holder = new Holder();
        holder.setDni("12345678");
        holder.setBirthDate(today.minusYears(30));

        License originalLicense = new License();
        originalLicense.setId(1L);
        originalLicense.setDni("12345678");
        originalLicense.setCuit("20123456780");
        originalLicense.setLast_name("Pérez");
        originalLicense.setFirst_name("Juan");
        originalLicense.setAddress("Calle 123");
        originalLicense.setCity("Santa Fe");
        originalLicense.setBirthDate(today.minusYears(30));
        originalLicense.setIssuanceDate(today.minusYears(1));
        originalLicense.setExpirationDate(today.plusYears(4));
        originalLicense.setLicenseClasses(List.of(LicenseClass.B));
        originalLicense.setIsValid(true);
        originalLicense.setVersion(1);
        originalLicense.setObvservations("Observación original");
        originalLicense.setIsDonor(true);
        originalLicense.setHolder(holder);

        User administrativo = new User();
        administrativo.setId(1L);
        administrativo.setUsername("admin");
        // Mock repository behavior
        when(licenseRepository.findByDniAndExpirationDateGreaterThanEqualAndIsValidTrue("12345678", today))
                .thenReturn(Optional.empty()); // No hay licencia activa previa
        when(licenseRepository.findByDni("12345678")).thenReturn(List.of(originalLicense)); // Para calcularExpiracion
        when(licenseRepository.save(any(License.class))).thenAnswer(invocation -> {
            License saved = invocation.getArgument(0);
            saved.setId(2L); // Simular que se asigna un nuevo ID
            return saved;
        });

        // Mocks for pricing calculation
        LicensePricing tarifaB = new LicensePricing();
        tarifaB.setPrice(25);
        when(pricingRepository.findByLicenseClassAndValidityYears(LicenseClass.B, 5))
                .thenReturn(tarifaB);

        // Act
        licenseService.makeLicenseCopy(originalLicense, administrativo);
        // Assert
        verify(licenseRepository).save(argThat(newLicense -> newLicense.getDni().equals("12345678") &&
                newLicense.getCuit().equals("20123456780") &&
                newLicense.getLast_name().equals("Pérez") &&
                newLicense.getFirst_name().equals("Juan") &&
                newLicense.getAddress().equals("Calle 123") &&
                newLicense.getCity().equals("Santa Fe") &&
                newLicense.getBirthDate().equals(today.minusYears(30)) &&
                newLicense.getIssuanceDate().equals(today) && // Nueva fecha de emisión
                newLicense.getLicenseClasses().equals(List.of(LicenseClass.B)) &&
                newLicense.getIsValid().equals(true) &&
                newLicense.getVersion().equals(2) && // Versión incrementada
                newLicense.getObvservations().equals("Observación original") &&
                newLicense.getIsDonor().equals(true) &&
                newLicense.getHolder().equals(holder) &&
                newLicense.getUser().equals(administrativo) &&
                Double.compare(newLicense.getCost(), 50.0) == 0 // Cost should be LICENSE_COPY_COST for copies
        ));
    }

    @Test
    public void testMakeLicenseCopy_deberiaInvalidarLicenciaActivaAnterior() {
        // Arrange
        LocalDate today = LocalDate.now();

        Holder holder = new Holder();
        holder.setDni("87654321");
        holder.setBirthDate(today.minusYears(25));

        License originalLicense = new License();
        originalLicense.setId(1L);
        originalLicense.setDni("87654321");
        originalLicense.setVersion(3);
        originalLicense.setHolder(holder);
        originalLicense.setLicenseClasses(List.of(LicenseClass.A));
        originalLicense.setIssuanceDate(today.minusYears(2));
        originalLicense.setExpirationDate(today.plusYears(3));

        // Licencia activa existente que debe ser invalidada
        License licenciaActiva = new License();
        licenciaActiva.setId(5L);
        licenciaActiva.setDni("87654321");
        licenciaActiva.setIsValid(true);
        licenciaActiva.setExpirationDate(today.plusYears(2));

        User administrativo = new User();
        administrativo.setId(2L);
        administrativo.setUsername("admin2");
        // Mock repository behavior
        when(licenseRepository.findByDniAndExpirationDateGreaterThanEqualAndIsValidTrue("87654321", today))
                .thenReturn(Optional.of(licenciaActiva));
        when(licenseRepository.findByDni("87654321")).thenReturn(List.of(originalLicense)); // Para calcularExpiracion
        when(licenseRepository.save(any(License.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Mocks for pricing calculation
        LicensePricing tarifaA = new LicensePricing();
        tarifaA.setPrice(20);
        when(pricingRepository.findByLicenseClassAndValidityYears(LicenseClass.A, 5))
                .thenReturn(tarifaA);

        // Act
        licenseService.makeLicenseCopy(originalLicense, administrativo);
        // Assert
        // Verificar que se invalidó la licencia activa
        verify(licenseRepository)
                .save(argThat(license -> license.getDni().equals("87654321") && !license.getIsValid()));

        // Verificar que se creó la nueva licencia con versión incrementada
        verify(licenseRepository).save(argThat(newLicense -> newLicense.getDni().equals("87654321") &&
                newLicense.getVersion().equals(4) && // Original era 3, debe ser 4
                newLicense.getUser().equals(administrativo) &&
                newLicense.getIssuanceDate().equals(today)));

        // Se debe haber llamado save 2 veces: una para invalidar, otra para crear
        verify(licenseRepository, times(2)).save(any(License.class));
    }

    @Test
    public void testMakeLicenseCopy_deberiaCopiarColeccionLicenseClassesSinCompartirReferencia() {
        // Arrange
        LocalDate today = LocalDate.now();

        Holder holder = new Holder();
        holder.setDni("11111111");
        holder.setBirthDate(today.minusYears(28));

        List<LicenseClass> originalClasses = List.of(LicenseClass.A, LicenseClass.B);
        License originalLicense = new License();
        originalLicense.setId(1L);
        originalLicense.setDni("11111111");
        originalLicense.setVersion(1);
        originalLicense.setHolder(holder);
        originalLicense.setLicenseClasses(originalClasses);
        originalLicense.setIssuanceDate(today.minusYears(1));
        originalLicense.setExpirationDate(today.plusYears(4));

        User administrativo = new User();
        administrativo.setId(1L);
        administrativo.setUsername("admin");
        // Mock repository behavior
        when(licenseRepository.findByDniAndExpirationDateGreaterThanEqualAndIsValidTrue("11111111", today))
                .thenReturn(Optional.empty());
        when(licenseRepository.findByDni("11111111")).thenReturn(List.of(originalLicense)); // Para calcularExpiracion
        when(licenseRepository.save(any(License.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Mocks for pricing calculation
        LicensePricing tarifaA = new LicensePricing();
        tarifaA.setPrice(20);
        LicensePricing tarifaB = new LicensePricing();
        tarifaB.setPrice(25);
        when(pricingRepository.findByLicenseClassAndValidityYears(LicenseClass.A, 5))
                .thenReturn(tarifaA);
        when(pricingRepository.findByLicenseClassAndValidityYears(LicenseClass.B, 5))
                .thenReturn(tarifaB);

        // Act
        licenseService.makeLicenseCopy(originalLicense, administrativo);

        // Assert
        verify(licenseRepository).save(argThat(newLicense -> {
            // Verificar que las clases son iguales en contenido
            boolean sameContent = newLicense.getLicenseClasses().equals(originalClasses);
            // Verificar que NO es la misma referencia de objeto
            boolean differentReference = newLicense.getLicenseClasses() != originalClasses;
            return sameContent && differentReference;
        }));
    }

    @Test
    public void testMakeLicenseCopy_deberiaCalcularNuevaExpiracionYCosto() {
        // Arrange
        LocalDate today = LocalDate.now();

        Holder holder = new Holder();
        holder.setDni("22222222");
        holder.setBirthDate(today.minusYears(35)); // 35 años, debe tener vigencia de 5 años

        License originalLicense = new License();
        originalLicense.setId(1L);
        originalLicense.setDni("22222222");
        originalLicense.setVersion(2);
        originalLicense.setHolder(holder);
        originalLicense.setLicenseClasses(List.of(LicenseClass.C));
        originalLicense.setIssuanceDate(today.minusYears(3));
        originalLicense.setExpirationDate(today.plusYears(2)); // Expiración original
        originalLicense.setCost(100.0); // Costo original

        User administrativo = new User();
        administrativo.setId(1L);
        administrativo.setUsername("admin");
        // Mock repository behavior
        when(licenseRepository.findByDniAndExpirationDateGreaterThanEqualAndIsValidTrue("22222222", today))
                .thenReturn(Optional.empty());
        when(licenseRepository.findByDni("22222222")).thenReturn(List.of(originalLicense)); // Ya tiene licencia previa
        when(licenseRepository.save(any(License.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Mocks for pricing calculation
        LicensePricing tarifaC = new LicensePricing();
        tarifaC.setPrice(50);
        when(pricingRepository.findByLicenseClassAndValidityYears(LicenseClass.C, 5))
                .thenReturn(tarifaC);

        // Act
        licenseService.makeLicenseCopy(originalLicense, administrativo);
        // Assert
        verify(licenseRepository).save(argThat(newLicense -> {
            // Verificar nueva fecha de emisión
            boolean correctIssuanceDate = newLicense.getIssuanceDate().equals(today);

            // Verificar nueva fecha de expiración (debe ser recalculada, no copiada)
            boolean correctExpirationDate = newLicense.getExpirationDate().equals(today.plusYears(5));

            // Verificar que el costo fue recalculado - for copies it should be
            // LICENSE_COPY_COST (50.0)
            boolean correctCost = Double.compare(newLicense.getCost(), 50.0) == 0;

            return correctIssuanceDate && correctExpirationDate && correctCost;
        }));
    }

    @Test
    public void testIsExpired_cuandoLicenciaEstaVencida_deberiaRetornarTrue() {
        // Arrange
        LocalDate today = LocalDate.now();

        License expiredLicense = new License();
        expiredLicense.setExpirationDate(today.minusDays(1)); // Expired yesterday

        // Act
        boolean result = expiredLicense.isExpired();

        // Assert
        assertTrue(result, "License should be expired when expiration date is in the past");
    }

    @Test
    public void testIsExpired_cuandoLicenciaNoEstaVencida_deberiaRetornarFalse() {
        // Arrange
        LocalDate today = LocalDate.now();

        License validLicense = new License();
        validLicense.setExpirationDate(today.plusDays(1)); // Expires tomorrow

        // Act
        boolean result = validLicense.isExpired();

        // Assert
        assertFalse(result, "License should not be expired when expiration date is in the future");
    }

    @Test
    public void testIsExpired_cuandoLicenciaVenceHoy_deberiaRetornarFalse() {
        // Arrange
        LocalDate today = LocalDate.now();

        License todayExpiringLicense = new License();
        todayExpiringLicense.setExpirationDate(today); // Expires today

        // Act
        boolean result = todayExpiringLicense.isExpired();

        // Assert
        assertFalse(result, "License should not be expired when expiration date is today");
    }

    @Test
    public void testIsCopy_cuandoVersionEsMayorA1_deberiaRetornarTrue() {
        // Arrange
        License copyLicense = new License();
        copyLicense.setVersion(2); // This is a copy

        // Act
        boolean result = copyLicense.isCopy();

        // Assert
        assertTrue(result, "License should be considered a copy when version > 1");
    }

    @Test
    public void testIsCopy_cuandoVersionEs1_deberiaRetornarFalse() {
        // Arrange
        License originalLicense = new License();
        originalLicense.setVersion(1); // Original license

        // Act
        boolean result = originalLicense.isCopy();

        // Assert
        assertFalse(result, "License should not be considered a copy when version = 1");
    }

    @Test
    public void testMakeLicenseCopy_cuandoLicenciaOriginalEsValida_deberiaCrearCopia() {
        // Arrange
        LocalDate today = LocalDate.now();

        Holder holder = new Holder();
        holder.setDni("12345678");
        holder.setBirthDate(today.minusYears(30));

        License validOriginalLicense = new License();
        validOriginalLicense.setId(1L);
        validOriginalLicense.setDni("12345678");
        validOriginalLicense.setIsValid(true); // Valid
        validOriginalLicense.setExpirationDate(today.plusYears(2)); // Not expired
        validOriginalLicense.setVersion(1); // Original
        validOriginalLicense.setHolder(holder);
        validOriginalLicense.setLicenseClasses(List.of(LicenseClass.B));

        User administrativo = new User();
        administrativo.setId(1L);
        administrativo.setUsername("admin");

        // Mock repository behavior
        when(licenseRepository.findByDniAndExpirationDateGreaterThanEqualAndIsValidTrue("12345678", today))
                .thenReturn(Optional.empty());
        when(licenseRepository.findByDni("12345678")).thenReturn(List.of(validOriginalLicense));
        when(licenseRepository.save(any(License.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        licenseService.makeLicenseCopy(validOriginalLicense, administrativo);

        // Assert
        verify(licenseRepository).save(argThat(newLicense ->
                newLicense.getDni().equals("12345678") &&
                        newLicense.getIsValid().equals(true) &&
                        newLicense.getVersion().equals(2) && // Version should be incremented
                        newLicense.isCopy() && // Should be identified as a copy
                        newLicense.getUser().equals(administrativo) &&
                        newLicense.getIssuanceDate().equals(today) // New issuance date
        ));
    }

    @Test
    public void testLicenseValidation_escenarioCompleto_licenciaVencidaVsValida() {
        // Arrange
        LocalDate today = LocalDate.now();

        Holder holder = new Holder();
        holder.setDni("99999999");
        holder.setBirthDate(today.minusYears(25));

        // Expired license
        License expiredLicense = new License();
        expiredLicense.setId(1L);
        expiredLicense.setDni("99999999");
        expiredLicense.setIsValid(true);
        expiredLicense.setExpirationDate(today.minusDays(30)); // Expired 30 days ago
        expiredLicense.setVersion(1);
        expiredLicense.setHolder(holder);

        // Valid license
        License validLicense = new License();
        validLicense.setId(2L);
        validLicense.setDni("99999999");
        validLicense.setIsValid(true);
        validLicense.setExpirationDate(today.plusYears(3)); // Valid for 3 more years
        validLicense.setVersion(1);
        validLicense.setHolder(holder);

        // Act & Assert for expired license
        assertTrue(expiredLicense.isExpired(), "License should be expired");
        assertFalse(expiredLicense.isCopy(), "Should not be a copy (version 1)");

        // Act & Assert for valid license
        assertFalse(validLicense.isExpired(), "License should not be expired");
        assertFalse(validLicense.isCopy(), "Should not be a copy (version 1)");

        // Test that we can identify the business logic correctly
        boolean canCopyExpired = !expiredLicense.isExpired() && expiredLicense.getIsValid();
        boolean canCopyValid = !validLicense.isExpired() && validLicense.getIsValid();

        assertFalse(canCopyExpired, "Should not be able to copy expired license");
        assertTrue(canCopyValid, "Should be able to copy valid, non-expired license");
    }
}