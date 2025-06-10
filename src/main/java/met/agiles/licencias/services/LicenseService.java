package met.agiles.licencias.services;

import met.agiles.licencias.enums.LicenseClass;
import met.agiles.licencias.persistance.models.Holder;
import met.agiles.licencias.persistance.models.License;
import met.agiles.licencias.persistance.models.LicensePricing;
import met.agiles.licencias.persistance.repository.LicensePricingRepository;
import met.agiles.licencias.persistance.repository.LicenseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;

@Service
public class LicenseService {
    
    @Autowired
    private LicenseRepository licenseRepository;

    @Autowired
    private LicensePricingRepository licensePricingRepository;

    public List<License> getAllLicenses() {
        return licenseRepository.findAll();
    }
    
    public License getLicenseById(Long id) {
        return licenseRepository.findById(id).orElse(null);
    }
    
    public License createLicense(License license) {
        this.calcularExpiracion(license);
        this.calcularCostoTotal(license);
        return licenseRepository.save(license);
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
            total += licensePricing.getPrice();
        }
        return total + LicensePricing.getBasePrice(); //+ gastos administrativos
    }

    public LicenseService(LicensePricingRepository pricingRepository, LicenseRepository licenseRepository) {
        this.licensePricingRepository = pricingRepository;
        this.licenseRepository = licenseRepository;
    }

    public void calcularExpiracion(License license) {

        LocalDate today = LocalDate.now();
        Holder holder = license.getHolder();

            if (licenseRepository.findByDni(holder.getDni()).isEmpty() && holder.getEdad() < 21) {
                license.setExpirationDate(today.plusYears(1));
                license.setObvservations((license.getObvservations() != null ? license.getObvservations() + "\n" : "") + "Principiante por primeros 6 meses.");
                System.out.println("1");
            } else if (holder.getEdad() < 21) {
                license.setExpirationDate(today.plusYears(3));
                System.out.println("2");
            } else if (holder.getEdad() >= 21 && holder.getEdad() < 46) {
                license.setExpirationDate(today.plusYears(5));
                System.out.println("3");
            } else if (holder.getEdad() >= 46 && holder.getEdad() < 60) {
                license.setExpirationDate(today.plusYears(4));
                System.out.println("4");
            } else if (holder.getEdad() >= 60 && holder.getEdad() < 70) {
                license.setExpirationDate(today.plusYears(3));
                System.out.println("5");
            } else {
                license.setExpirationDate(today.plusYears(1));
                System.out.println("6");
            }
    }

    public boolean isValidBirthDateWindow(LocalDate birthDate) {
        LocalDate today = LocalDate.now();
        LocalDate thisYearBirthday = birthDate.withYear(today.getYear());
        LocalDate oneMonthBefore = thisYearBirthday.minusMonths(1);

        return ( !today.isBefore(oneMonthBefore) && !today.isAfter(thisYearBirthday) );
    }

    public boolean isValidAge(LocalDate birthDate, List<LicenseClass> licenseClasses) {
        int age = Period.between(birthDate,LocalDate.now()).getYears();
        //Log age
        System.out.println("Age: " + age);

        for (LicenseClass licenseClass : licenseClasses) {
            // If licenseClass if C, D or E, then the holder must be at least 21 years old. Else he must be at least 17.
            if (licenseClass == LicenseClass.C || licenseClass == LicenseClass.D || licenseClass == LicenseClass.E) {
                if (age < 21) {
                    return false;
                }
            } else {
                if (age < 17) {
                    return false;
                }
            }
        }
        return true;
    }

    public boolean isValidFirstTimeForProfessionalLicense(String holderDni, LocalDate birthDate, List<LicenseClass> licenseClasses) {
        int age = Period.between(birthDate,LocalDate.now()).getYears();

        if (licenseClasses.contains(LicenseClass.C) || licenseClasses.contains(LicenseClass.D) || licenseClasses.contains(LicenseClass.E)) {
            List<License> licenses = licenseRepository.findByDni(holderDni);
            boolean hasValidBClassLicense = false;
            for (License license : licenses) {
                if (license.getLicenseClasses().contains(LicenseClass.B) && license.getIssuanceDate().isBefore(LocalDate.now().minusYears(1))) {
                    hasValidBClassLicense = true;
                }
            }
            boolean hasPreviousProfessionalLicense = false;
            for (License license : licenses) {
                if (license.getLicenseClasses().contains(LicenseClass.C) || license.getLicenseClasses().contains(LicenseClass.D) || license.getLicenseClasses().contains(LicenseClass.E)) {
                    hasPreviousProfessionalLicense = true;
                }
            }

            if(hasPreviousProfessionalLicense) return true; // Already has a professional license
            if(hasValidBClassLicense && age<=65) return true; // First time making a professional license

            // Log
            System.out.println("Has valid B class license: " + hasValidBClassLicense);
            System.out.println("Age: " + age);
            System.out.println("Has previous professional license: " + hasPreviousProfessionalLicense);

            return false;
        }
        return true; // Not a professional license
    }





}