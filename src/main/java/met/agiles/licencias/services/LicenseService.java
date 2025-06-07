package met.agiles.licencias.services;

import met.agiles.licencias.persistance.models.License;
import met.agiles.licencias.persistance.repository.LicenseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LicenseService {
    
    @Autowired
    private LicenseRepository licenseRepository;
    
    public List<License> getAllLicenses() {
        return licenseRepository.findAll();
    }
    
    public License getLicenseById(Long id) {
        return licenseRepository.findById(id).orElse(null);
    }
    
    public License createLicense(License license) {
        return licenseRepository.save(license);
    }
    
    public License updateLicense(License license) {
        return licenseRepository.save(license);
    }
    
    public void deleteLicense(Long id) {
        licenseRepository.deleteById(id);
    }
}