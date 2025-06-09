package met.agiles.licencias.persistance.models;

import java.sql.Date;
import java.util.List;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import met.agiles.licencias.enums.LicenseClass;

@Entity
@Table(name = "licenses", schema="public")
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
    private Holder holder; // Refers to the license holder. Current data of the holder can be different than the data on the license.

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
    private String address_name;

    @Column(nullable = false)
    private String address_number;
    
    @Column(nullable = false)
    private String city;

    @Column(nullable = false)
    private Date birthDate;

    @Column(nullable = false)
    private Date issuanceDate;

    @Column(nullable = false)
    private Date expirationDate;

    @ElementCollection
    @CollectionTable(
        name = "license_classes",
        joinColumns = @JoinColumn(name = "license_id")
    )
    @Column(name = "license_class")
    @Enumerated(EnumType.STRING)
    private List<LicenseClass> licenseClasses;

    @Column()
    private String obvservations;

    @Column()
    private Boolean isDonor;

    @Column()
    private int vigency = 3; // Number of years the license is valid

    @Column()
    private double cost; // Cost of the license, can be used to calculate the price of the license





}