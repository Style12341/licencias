package met.agiles.licencias.persistance.models;

import jakarta.persistence.*;
import met.agiles.licencias.enums.LicenseClass;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class LicensePricing {

    public static final float BASE_PRICE = 8;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private LicenseClass licenseClass;

    private int validityYears;
    private double price;

    public static float getBasePrice() {
        return BASE_PRICE;
    }

}
