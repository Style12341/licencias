package met.agiles.licencias.persistance.models;

import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.ColumnDefault;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import met.agiles.licencias.enums.LicenseClass;

@Entity
@Table(name = "licenses", schema = "public")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class License {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // License metadata
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user; // User that created the license

    @ManyToOne
    @JoinColumn(name = "holder_id")
    private Holder holder; // Refers to the license holder. Current data of the holder can be different
                           // than the data on the license.

    // Printed license data
    @Column(nullable = false)
    private String dni;

    @Column(nullable = false)
    private String cuit;

    @Column(nullable = false)
    private String last_name;

    @Column(nullable = false)
    private String first_name;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false)
    private String city;

    @Column(nullable = false)
    private LocalDate birthDate;

    @Column(nullable = false)
    private LocalDate issuanceDate;

    @Column(nullable = false)
    private LocalDate expirationDate;

    @ElementCollection
    @CollectionTable(name = "license_classes", joinColumns = @JoinColumn(name = "license_id"))
    @Column(name = "license_class")
    @Enumerated(EnumType.STRING)
    private List<LicenseClass> licenseClasses;

    @Column()
    @ColumnDefault("true")
    private Boolean isValid = true;

    @Column()
    @ColumnDefault("1")
    private Integer version = 1; // Version of the license, used for updates

    @Column()
    private String obvservations;

    @Column()
    private Boolean isDonor;

    public int getVigency() {
        Period periodo = Period.between(issuanceDate, expirationDate);
        return periodo.getYears();
    }

    @Column()
    private double cost; // Total cost of the license, including administrative fees

    public void copyLicenseAttributes(License license) {
        this.dni = license.getDni();
        this.cuit = license.getCuit();
        this.last_name = license.getLast_name();
        this.first_name = license.getFirst_name();
        this.address = license.getAddress();
        this.city = license.getCity();
        this.birthDate = license.getBirthDate();
        this.issuanceDate = license.getIssuanceDate();
        this.expirationDate = license.getExpirationDate();
        // Create a new ArrayList to avoid shared collection references
        this.licenseClasses = license.getLicenseClasses() != null ? new ArrayList<>(license.getLicenseClasses()) : null;
        this.isValid = license.getIsValid();
        this.obvservations = license.getObvservations();
        this.isDonor = license.getIsDonor();
        this.version = license.getVersion();
        // Copy holder reference (same holder entity can be shared)
        this.holder = license.getHolder();
    }

    public boolean isExpired() {
        return LocalDate.now().isAfter(this.expirationDate);
    }

    public boolean isCopy() {
        return this.version > 1;
    }
}