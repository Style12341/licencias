package met.agiles.licencias.persistance.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import met.agiles.licencias.enums.PaymentMethod;

import java.time.LocalDate;

@Entity
@Table(name = "payment_receipts")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentReceipt {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "license_id")
    private License license;

    @ManyToOne
    @JoinColumn(name = "administrativo_id")
    private User administrativo; // quien llevo a cabo la impresion de la licencia

    @Column(nullable = false)
    private PaymentMethod paymentMethod;

    @Column(nullable = false)
    private LocalDate paymentDate; // Fecha de pago
}
