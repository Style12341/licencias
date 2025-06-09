package met.agiles.licencias.persistance.repository;

import met.agiles.licencias.enums.LicenseClass;
import met.agiles.licencias.persistance.models.LicensePricing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LicensePricingRepository extends JpaRepository<LicensePricing, Long> {

    LicensePricing findByLicenseClassAndValidityYears(LicenseClass licenseClass, int validityYears);

}
