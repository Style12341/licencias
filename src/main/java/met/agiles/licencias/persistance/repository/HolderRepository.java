package met.agiles.licencias.persistance.repository;

import met.agiles.licencias.persistance.models.Holder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HolderRepository extends JpaRepository<Holder, String>{
    
}
