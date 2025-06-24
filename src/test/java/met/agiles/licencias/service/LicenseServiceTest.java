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

}