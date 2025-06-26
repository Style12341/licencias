package met.agiles.licencias.persistance.repository;

import met.agiles.licencias.persistance.models.License;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface LicenseRepository extends JpaRepository<License, Long> {

    List<License> findByDni(String dni);

    List<License> findByExpirationDateBetween(LocalDate startDate, LocalDate endDate);

    Optional<License> findByDniAndExpirationDateGreaterThanEqualAndIsValidTrue(String dni, LocalDate expirationDate);

}