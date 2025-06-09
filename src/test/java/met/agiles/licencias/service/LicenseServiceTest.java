package met.agiles.licencias.service;

import met.agiles.licencias.enums.LicenseClass;
import met.agiles.licencias.persistance.models.License;
import met.agiles.licencias.persistance.models.LicensePricing;
import met.agiles.licencias.persistance.repository.LicensePricingRepository;
import met.agiles.licencias.persistance.repository.LicenseRepository;
import met.agiles.licencias.services.LicenseService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

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
        license.setVigency(3); // años

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
        assertEquals(resultado, license.getCost()); // también seteó el costo en la licencia
    }

    @Test
    void calcularCostoTotal_lanzaExcepcionSiNoEncuentraTarifa() {
        // Arrange
        License license = new License();
        license.setLicenseClasses(List.of(LicenseClass.C));
        license.setVigency(5);

        when(pricingRepository.findByLicenseClassAndValidityYears(LicenseClass.C, 5))
                .thenReturn(null); // Simula que no se encuentra tarifa

        // Act & Assert
        assertThrows(NullPointerException.class, () -> {licenseService.calcularCostoTotal(license);});
    }
}
