package met.agiles.licencias.persistance.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import met.agiles.licencias.enums.LicenseClass;

import java.time.LocalDate;

@Entity
@Table(name = "holders")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Holder {
    
    @Id
    private String dni;

    @Column(nullable = false)
    private String cuil;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String lastName;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false)
    private String city;

    @Column(nullable = false)
    private LocalDate birthDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = true) // Por comentario alex TODO: Revisar
    private LicenseClass licenseClass; // Clase de licencia, Ej: A, B, C, D, E, F, G

    @Column(nullable = false)
    private String bloodType;

    // @Column(nullable = false)
    // private boolean donor = false; deberia ser asi.

    @Column(nullable = false)
    private boolean donor;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "administrative_id", nullable = false)
    private User administrative;  // TODO: el administrativo es el user no?

}
