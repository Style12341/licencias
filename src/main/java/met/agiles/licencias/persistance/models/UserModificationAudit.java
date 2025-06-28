package met.agiles.licencias.persistance.models;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_modification_audit")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserModificationAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // El usuario que recibió el cambio
    @ManyToOne
    @JoinColumn(name = "target_user_id", nullable = false)
    private User targetUser;

    // El administrativo que hizo la modificación
    @ManyToOne
    @JoinColumn(name = "performed_by_user_id", nullable = false)
    private User performedBy;

    // Fecha y hora de la modificación
    @Column(name = "performed_at", nullable = false)
    private LocalDateTime performedAt;

}
