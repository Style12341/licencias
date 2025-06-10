package met.agiles.licencias.persistance.repository;

import met.agiles.licencias.persistance.models.License;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LicenseRepository extends JpaRepository<License, Long> {

    List<License> findByDni(String dni);

}