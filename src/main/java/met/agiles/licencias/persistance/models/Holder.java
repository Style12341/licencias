package met.agiles.licencias.persistance.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "holders")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Holder {
    
    @Id
    private String dni;
    
    @Column(nullable = false)
    private String name;
    
}
