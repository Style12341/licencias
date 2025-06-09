package met.agiles.licencias.dto;

import lombok.Data;
import met.agiles.licencias.enums.LicenseClass;

import java.time.LocalDate;

@Data
public class HolderRequestDto {
    private String dni;
    private String cuil;
    private String name;
    private String lastName;
    private String address;
    private String city;
    private LocalDate birthDate;
    private LicenseClass licenseClass;
    private String bloodType;
    private boolean donor;
}

