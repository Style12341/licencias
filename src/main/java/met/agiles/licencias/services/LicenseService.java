package met.agiles.licencias.services;

import met.agiles.licencias.enums.LicenseClass;
import met.agiles.licencias.persistance.models.License;
import met.agiles.licencias.persistance.models.LicensePricing;
import met.agiles.licencias.persistance.repository.LicensePricingRepository;
import met.agiles.licencias.persistance.repository.LicenseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.metrics.SystemMetricsAutoConfiguration;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LicenseService {
    
    @Autowired
    private LicenseRepository licenseRepository;

    @Autowired
    private LicensePricingRepository licensePricingRepository;
    @Autowired
    private SystemMetricsAutoConfiguration systemMetricsAutoConfiguration;

    public List<License> getAllLicenses() {
        return licenseRepository.findAll();
    }
    
    public License getLicenseById(Long id) {
        return licenseRepository.findById(id).orElse(null);
    }
    
    public License createLicense(License license) {license.setCost(this.calcularCostoTotal(license)); return licenseRepository.save(license);
    }
    
    public License updateLicense(License license) {
        return licenseRepository.save(license);
    }
    
    public void deleteLicense(Long id) {
        licenseRepository.deleteById(id);
    }

    public double calcularCostoTotal(License license) {
        double total = 0;
        for (LicenseClass clase : license.getLicenseClasses()) {
            LicensePricing licensePricing = licensePricingRepository.findByLicenseClassAndValidityYears(clase, license.getVigency());
            System.out.println("Calculating total cost for license class: " + clase + " with price: " + licensePricing.getPrice());
            total += licensePricing.getPrice();
        }
        System.out.println("Total cost calculated: " + total);
        license.setCost(total + LicensePricing.getBasePrice()); // gastos administrativos
        return total + LicensePricing.getBasePrice(); // gastos administrativos
    }

}