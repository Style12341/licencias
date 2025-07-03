package met.agiles.licencias.services;

import jakarta.transaction.Transactional;
import met.agiles.licencias.controllers.AdministrativoController;
import met.agiles.licencias.enums.BloodType;
import met.agiles.licencias.enums.LicenseClass;
import met.agiles.licencias.enums.PaymentMethod;
import met.agiles.licencias.persistance.models.*;
import met.agiles.licencias.persistance.repository.LicensePricingRepository;
import met.agiles.licencias.persistance.repository.LicenseRepository;
import met.agiles.licencias.persistance.repository.PaymentReceiptRepository;
import met.agiles.licencias.persistance.repository.UsuarioRepository;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import org.slf4j.Logger;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.Optional;

@Service
public class LicenseService {
    static final double LICENSE_COPY_COST = 50.0; // Cost for making a copy of a license
    @Autowired
    private LicenseRepository licenseRepository;

    @Autowired
    private LicensePricingRepository licensePricingRepository;

    @Autowired
    private PaymentReceiptRepository paymentReceiptRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    public List<License> getAllLicenses() {
        return licenseRepository.findAll();
    }

    public License getLicenseById(Long id) {
        return licenseRepository.findById(id).orElse(null);
    }

    @Transactional
    public License createLicense(License license) {
        this.setExpiracionLicencia(license);
        double cost = this.calcularCostoTotal(license);
        license.setCost(cost);
        Holder holder = license.getHolder();
        this.invalidateActiveLicense(holder);
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
        if (license.isCopy()) {
            return LICENSE_COPY_COST; // If it's a copy, return the fixed cost
        }
        for (LicenseClass clase : license.getLicenseClasses()) {
            LicensePricing licensePricing = licensePricingRepository.findByLicenseClassAndValidityYears(clase, license.getVigency());
            total += licensePricing.getPrice();
        }
        return total + LicensePricing.getBasePrice(); // + gastos administrativos
    }

    public void makeLicenseCopy(License originalLicense, User administrativo) {
        License newLicense = new License();
        newLicense.copyLicenseAttributes(originalLicense); // This now copies everything including holder
        newLicense.setVersion(originalLicense.getVersion() + 1);
        newLicense.setUser(administrativo);
        newLicense.setIssuanceDate(LocalDate.now());
        this.createLicense(newLicense); // This will also calculate expiration and cost
    }

    public LicenseService(LicensePricingRepository pricingRepository, LicenseRepository licenseRepository) {
        this.licensePricingRepository = pricingRepository;
        this.licenseRepository = licenseRepository;
    }

    public void setExpiracionLicencia(License license) {
        LocalDate today = LocalDate.now();
        license.setIssuanceDate(today);

        Holder holder = license.getHolder();
        LocalDate birthDate = holder.getBirthDate();
        int edad = holder.getEdad();

        int añosVigencia;

        if (licenseRepository.findByDni(holder.getDni()).isEmpty() && edad < 21) {
            añosVigencia = 1;
        } else if (edad < 21) {
            añosVigencia = 3;
        } else if (edad < 46) {
            añosVigencia = 5;
        } else if (edad < 60) {
            añosVigencia = 4;
        } else if (edad < 70) {
            añosVigencia = 3;
        } else {
            añosVigencia = 1;
        }

        LocalDate expirationDate = birthDate.withYear(today.getYear() + añosVigencia);
        license.setExpirationDate(expirationDate);
    }


    public boolean isValidBirthDateWindow(LocalDate birthDate) {
        LocalDate today = LocalDate.now();
        LocalDate thisYearBirthday = birthDate.withYear(today.getYear());
        LocalDate oneMonthBefore = thisYearBirthday.minusMonths(1);

        return (!today.isBefore(oneMonthBefore) && !today.isAfter(thisYearBirthday));
    }

    public boolean isValidAge(LocalDate birthDate, List<LicenseClass> licenseClasses) {
        int age = Period.between(birthDate, LocalDate.now()).getYears();
        // Log age
        System.out.println("Age: " + age);

        for (LicenseClass licenseClass : licenseClasses) {
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

    public boolean isValidFirstTimeForProfessionalLicense(String holderDni, LocalDate birthDate,
            List<LicenseClass> licenseClasses) {
        int age = Period.between(birthDate, LocalDate.now()).getYears();

        if (licenseClasses.contains(LicenseClass.C) || licenseClasses.contains(LicenseClass.D)
                || licenseClasses.contains(LicenseClass.E)) {
            List<License> licenses = licenseRepository.findByDni(holderDni);
            boolean hasValidBClassLicense = false;
            for (License license : licenses) {
                if (license.getLicenseClasses().contains(LicenseClass.B)
                        && license.getIssuanceDate().isBefore(LocalDate.now().minusYears(1))) {
                    hasValidBClassLicense = true;
                }
            }
            boolean hasPreviousProfessionalLicense = false;
            for (License license : licenses) {
                if (license.getLicenseClasses().contains(LicenseClass.C)
                        || license.getLicenseClasses().contains(LicenseClass.D)
                        || license.getLicenseClasses().contains(LicenseClass.E)) {
                    hasPreviousProfessionalLicense = true;
                }
            }

            if (hasPreviousProfessionalLicense)
                return true; // Already has a professional license
            if (hasValidBClassLicense && age <= 65)
                return true; // First time making a professional license

            // Log
            System.out.println("Has valid B class license: " + hasValidBClassLicense);
            System.out.println("Age: " + age);
            System.out.println("Has previous professional license: " + hasPreviousProfessionalLicense);

            return false;
        }
        return true; // Not a professional license
    }

    public List<License> searchFilteredLicenses(String dni, String apellido, String nombre,
                                                BloodType bloodType, Boolean isDonor, Boolean isValid, String orden) {
        LocalDate today = LocalDate.now();
        List<License> todas = licenseRepository.findAll();

        return todas.stream()
                .filter(l -> l.getExpirationDate().isAfter(today)) // Solo licencias vigentes
                .filter(l -> dni == null || dni.isBlank() || l.getDni().contains(dni))
                .filter(l -> apellido == null || apellido.isBlank() ||
                        l.getLast_name().toLowerCase().contains(apellido.toLowerCase()))
                .filter(l -> nombre == null || nombre.isBlank() ||
                        l.getFirst_name().toLowerCase().contains(nombre.toLowerCase()))
                .filter(l -> bloodType == null || l.getHolder().getBloodType() == bloodType)
                .filter(l -> isDonor == null || l.getHolder().isDonor() == isDonor)
                .filter(l -> isValid == null || l.getIsValid() == isValid)
                .sorted((l1, l2) -> {
                    if ("desc".equalsIgnoreCase(orden)) {
                        return l2.getIssuanceDate().compareTo(l1.getIssuanceDate());
                    } else {
                        return l1.getIssuanceDate().compareTo(l2.getIssuanceDate());
                    }
                })
                .toList();
    }

    @Transactional
    public void invalidateActiveLicense(Holder holder) {
        String dni = holder.getDni();
        Optional<License> activeLicenseOpt = this.getActiveLicenseByHolderDni(dni);
        if (activeLicenseOpt.isPresent()) {
            // Log that the active license is being invalidated
            Logger logger = LoggerFactory.getLogger(AdministrativoController.class);
            logger.info("Invalidating active license for holder with DNI: " + dni);
            License activeLicense = activeLicenseOpt.get();
            activeLicense.setIsValid(false); // Invalidate the active license
            licenseRepository.save(activeLicense); // Save the changes
        }
    }

    public boolean isFirstLicense(String dni) {
        List<License> licenses = licenseRepository.findByDni(dni);
        return licenses.isEmpty();
    }

    public Optional<License> getActiveLicenseByHolderDni(String dni) {
        return licenseRepository.findByDniAndExpirationDateGreaterThanEqualAndIsValidTrue(dni, LocalDate.now());
    }

    @Transactional
    public License assignPaymentToLicense(Long licenseId, PaymentMethod paymentMethod) {
        License license = licenseRepository.findById(licenseId)
                .orElseThrow(() -> new RuntimeException("Licencia no encontrada con ID: " + licenseId));

        if(paymentReceiptRepository.findByLicenseId(licenseId) == null) {
            PaymentReceipt paymentReceipt = new PaymentReceipt();
            paymentReceipt.setPaymentMethod(paymentMethod);

            // Set the user to the current user logged
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            User user = usuarioRepository.findByUsername(userDetails.getUsername()).orElse(null);
            paymentReceipt.setAdministrativo(user);

            paymentReceipt.setPaymentDate(LocalDate.now()); // Set the payment date to today

            if (paymentReceipt.getAdministrativo() == null) {
                throw new RuntimeException("Usuario administrativo no encontrado.");
            }
            if (paymentReceipt.getPaymentMethod() == null) {
                throw new RuntimeException("Método de pago no especificado.");
            }

            paymentReceipt.setLicense(license); // Associate the payment receipt with the license
            paymentReceiptRepository.save(paymentReceipt);
        }
        else {
            throw new RuntimeException("La licencia ya tiene un recibo de pago asociado.");
        }

        return licenseRepository.save(license); // Guardar la licencia actualizada
    }
}