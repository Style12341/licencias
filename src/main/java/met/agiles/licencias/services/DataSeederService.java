package met.agiles.licencias.services;

import met.agiles.licencias.dto.HolderRequestDto;
import met.agiles.licencias.enums.BloodType;
import met.agiles.licencias.enums.LicenseClass;
import met.agiles.licencias.enums.Role;
import met.agiles.licencias.persistance.models.Holder;
import met.agiles.licencias.persistance.models.License;
import met.agiles.licencias.persistance.models.LicensePricing;
import met.agiles.licencias.persistance.models.User;
import met.agiles.licencias.persistance.repository.LicensePricingRepository;
import met.agiles.licencias.persistance.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class DataSeederService implements CommandLineRunner {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private LicensePricingRepository licensePricingRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private HolderService holderService;
    @Autowired
    private LicenseService licenseService;

    @Override
    public void run(String... args) {
        seedAdministrator();
        seedAdministrativo();
        seedLicensePricing();
        seedHoldersAndLicenses();
    }

    private void seedAdministrator() {
        // Check if administrator already exists
        if (!usuarioRepository.existsByUsername("admin")) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRole(Role.ADMINISTRADOR);
            
            usuarioRepository.save(admin);
            
            System.out.println("✅ Administrator user created successfully:");
            System.out.println("   Username: admin");
            System.out.println("   Password: admin123");
            System.out.println("   Role: ADMINISTRADOR");
        } else {
            System.out.println("ℹ️ Administrator user already exists, skipping seeding.");
        }
    }

    private void seedAdministrativo() {
        // Check if administrativo already exists
        if (!usuarioRepository.existsByUsername("administrativo")) {
            User administrativo = new User();
            administrativo.setUsername("administrativo");
            administrativo.setPassword(passwordEncoder.encode("admin123"));
            administrativo.setRole(Role.ADMINISTRATIVO);
            administrativo.setProvincia("Buenos Aires");
            administrativo.setCiudad("La Plata");
            
            usuarioRepository.save(administrativo);
            
            System.out.println("✅ Administrativo user created successfully:");
            System.out.println("   Username: administrativo");
            System.out.println("   Password: admin123");
            System.out.println("   Role: ADMINISTRATIVO");
            System.out.println("   Provincia: Buenos Aires");
            System.out.println("   Ciudad: La Plata");
        } else {
            System.out.println("ℹ️ Administrativo user already exists, skipping seeding.");
        }
    }

    private void seedLicensePricing() {
        if (licensePricingRepository.count() == 0) {
            List<LicensePricing> pricings = List.of(
                    new LicensePricing(null, LicenseClass.A, 1, 20),
                    new LicensePricing(null, LicenseClass.A, 3, 25),
                    new LicensePricing(null, LicenseClass.A, 4, 30),
                    new LicensePricing(null, LicenseClass.A, 5, 40),

                    new LicensePricing(null, LicenseClass.B, 1, 20),
                    new LicensePricing(null, LicenseClass.B, 3, 25),
                    new LicensePricing(null, LicenseClass.B, 4, 30),
                    new LicensePricing(null, LicenseClass.B, 5, 40),

                    new LicensePricing(null, LicenseClass.C, 1, 23),
                    new LicensePricing(null, LicenseClass.C, 3, 30),
                    new LicensePricing(null, LicenseClass.C, 4, 35),
                    new LicensePricing(null, LicenseClass.C, 5, 47),

                    new LicensePricing(null, LicenseClass.D, 1, 0),
                    new LicensePricing(null, LicenseClass.D, 3, 0),
                    new LicensePricing(null, LicenseClass.D, 4, 0),
                    new LicensePricing(null, LicenseClass.D, 5, 0),

                    new LicensePricing(null, LicenseClass.E, 1, 29),
                    new LicensePricing(null, LicenseClass.E, 3, 39),
                    new LicensePricing(null, LicenseClass.E, 4, 44),
                    new LicensePricing(null, LicenseClass.E, 5, 59),

                    new LicensePricing(null, LicenseClass.F, 1, 0),
                    new LicensePricing(null, LicenseClass.F, 3, 0),
                    new LicensePricing(null, LicenseClass.F, 4, 0),
                    new LicensePricing(null, LicenseClass.F, 5, 0),

                    new LicensePricing(null, LicenseClass.G, 1, 20),
                    new LicensePricing(null, LicenseClass.G, 3, 25),
                    new LicensePricing(null, LicenseClass.G, 4, 30),
                    new LicensePricing(null, LicenseClass.G, 5, 40)
            );

            licensePricingRepository.saveAll(pricings);
            System.out.println("LicensePricing data seeded successfully.");

        } else {
            System.out.println("LicensePricing data already exists, skipping seeding.");
        }
    }

    private void seedHoldersAndLicenses() {
        if(holderService.getAllHolders().isEmpty()) {
            HolderRequestDto holder1 = new HolderRequestDto();
            holder1.setName("Juan");
            holder1.setLastName("Pérez");
            holder1.setDni("45058399");
            holder1.setCuil("20-45058399-7");
            holder1.setCity("Santa Fe");
            holder1.setAddress("Juan de Garay 1234");
            holder1.setBirthDate(LocalDate.of(1990, 5, 15));
            holder1.setBloodType(BloodType.A_NEGATIVE);
            holderService.createHolder(holder1, "administrativo");

            HolderRequestDto holder2 = new HolderRequestDto();
            holder2.setName("María");
            holder2.setLastName("Gómez");
            holder2.setDni("43555274");
            holder2.setCuil("20-43555274-5");
            holder2.setCity("Córdoba");
            holder2.setAddress("Av. Colón 5678");
            holder2.setBirthDate(LocalDate.of(1985, 8, 20));
            holder2.setBloodType(BloodType.O_POSITIVE);
            holderService.createHolder(holder2, "administrativo");
        } else {
            System.out.println("Holders and Licenses already exist, skipping seeding.");
        }

        if(licenseService.getAllLicenses().isEmpty()) {
            // Seed Licenses for the holders
            Holder holder1 = holderService.getHolderByDni("45058399");
            Holder holder2 = holderService.getHolderByDni("43555274");

            License license1 = new License();
            license1.setHolder(holder1);
            license1.setDni(holder1.getDni());
            license1.setCuit(holder1.getCuil());
            license1.setLast_name(holder1.getLastName());
            license1.setFirst_name(holder1.getName());
            license1.setAddress(holder1.getAddress());
            license1.setCity(holder1.getCity());
            license1.setBirthDate(holder1.getBirthDate());
            license1.setIssuanceDate(holder1.getBirthDate().withYear(LocalDate.now().getYear()));
            license1.setExpirationDate(LocalDate.now().plusYears(4));
            license1.setLicenseClasses(List.of(LicenseClass.A));
            license1.setCost(licenseService.calcularCostoTotal(license1));
            licenseService.createLicense(license1);

            License license2 = new License();
            license2.setHolder(holder1);
            license2.setDni(holder1.getDni());
            license2.setCuit(holder1.getCuil());
            license2.setLast_name(holder1.getLastName());
            license2.setFirst_name(holder1.getName());
            license2.setAddress(holder1.getAddress());
            license2.setCity(holder1.getCity());
            license2.setBirthDate(holder1.getBirthDate());
            license2.setIssuanceDate(holder1.getBirthDate().withYear(LocalDate.now().getYear()));
            license2.setExpirationDate(LocalDate.now().plusYears(4));
            license2.setLicenseClasses(List.of(LicenseClass.B));
            license2.setCost(licenseService.calcularCostoTotal(license2));
            licenseService.createLicense(license2);

            License license3 = new License();
            license3.setHolder(holder2);
            license3.setDni(holder2.getDni());
            license3.setCuit(holder2.getCuil());
            license3.setLast_name(holder2.getLastName());
            license3.setFirst_name(holder2.getName());
            license3.setAddress(holder2.getAddress());
            license3.setCity(holder2.getCity());
            license3.setBirthDate(holder2.getBirthDate());
            license3.setIssuanceDate(holder2.getBirthDate().withYear(LocalDate.now().getYear()));
            license3.setExpirationDate(LocalDate.now().plusYears(4));
            license3.setLicenseClasses(List.of(LicenseClass.A));
            license3.setCost(licenseService.calcularCostoTotal(license3));
            licenseService.createLicense(license3);

            License license4 = new License();
            license4.setHolder(holder2);
            license4.setDni(holder2.getDni());
            license4.setCuit(holder2.getCuil());
            license4.setLast_name(holder2.getLastName());
            license4.setFirst_name(holder2.getName());
            license4.setAddress(holder2.getAddress());
            license4.setCity(holder2.getCity());
            license4.setBirthDate(holder2.getBirthDate());
            license4.setIssuanceDate(holder2.getBirthDate().withYear(LocalDate.now().getYear()));
            license4.setExpirationDate(LocalDate.now().plusYears(4));
            license4.setLicenseClasses(List.of(LicenseClass.A, LicenseClass.B, LicenseClass.C));
            license4.setCost(licenseService.calcularCostoTotal(license4));
            licenseService.createLicense(license4);

            System.out.println("Licenses seeded successfully.");
        } else {
            System.out.println("Licenses already exist, skipping seeding.");
        }
    }
}