package met.agiles.licencias.persistance.repository;

import met.agiles.licencias.enums.Role;
import met.agiles.licencias.persistance.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByUsername(String username);
    
    boolean existsByUsername(String username);
    
    long countByRole(Role role);
}