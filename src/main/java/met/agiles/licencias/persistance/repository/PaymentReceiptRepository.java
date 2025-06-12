package met.agiles.licencias.persistance.repository;

import met.agiles.licencias.persistance.models.PaymentReceipt;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentReceiptRepository extends JpaRepository<PaymentReceipt, Long> {

}
