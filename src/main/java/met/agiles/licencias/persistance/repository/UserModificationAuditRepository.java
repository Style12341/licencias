package met.agiles.licencias.persistance.repository;

import met.agiles.licencias.persistance.models.UserModificationAudit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserModificationAuditRepository extends JpaRepository<UserModificationAudit, Long> {
}
