package met.agiles.licencias.services;

import met.agiles.licencias.enums.LicenseClass;
import met.agiles.licencias.enums.Role;
import met.agiles.licencias.persistance.models.LicensePricing;
import met.agiles.licencias.persistance.models.User;
import met.agiles.licencias.persistance.repository.LicensePricingRepository;
import met.agiles.licencias.persistance.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DataSeederService implements CommandLineRunner {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private LicensePricingRepository licensePricingRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        seedAdministrator();
        seedAdministrativo();
        seedLicensePricing();
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






}