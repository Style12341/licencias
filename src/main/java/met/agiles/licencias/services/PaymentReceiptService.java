package met.agiles.licencias.services;

import met.agiles.licencias.persistance.models.PaymentReceipt;
import met.agiles.licencias.persistance.repository.PaymentReceiptRepository; // Asume que tienes un repositorio para PaymentReceipt
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PaymentReceiptService {

    @Autowired
    private PaymentReceiptRepository paymentReceiptRepository;

    public PaymentReceipt savePaymentReceipt(PaymentReceipt paymentReceipt) {
        return paymentReceiptRepository.save(paymentReceipt);
    }

}