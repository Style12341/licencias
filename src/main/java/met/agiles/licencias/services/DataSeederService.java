package met.agiles.licencias.services;

import met.agiles.licencias.enums.Role;
import met.agiles.licencias.persistance.models.User;
import met.agiles.licencias.persistance.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class DataSeederService implements CommandLineRunner {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        seedAdministrator();
        seedAdministrativo();
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
}