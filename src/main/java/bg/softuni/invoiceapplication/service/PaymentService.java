package bg.softuni.invoiceapplication.service;

import bg.softuni.invoiceapplication.model.dto.PaymentCreateRequestDTO;
import bg.softuni.invoiceapplication.model.dto.PaymentEditRequestDTO;
import bg.softuni.invoiceapplication.model.entity.Payment;
import bg.softuni.invoiceapplication.model.enums.InvoiceCurrency;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface PaymentService {

    Payment createPayment(Payment payment);

    Payment createPayment(PaymentCreateRequestDTO paymentCreateRequestDTO);

    List<Payment> findAllPayments();

    List<Payment> findPaymentsByClientId(UUID clientId);

    PaymentEditRequestDTO getPaymentForEdit(UUID paymentId);

    void editPayment(PaymentEditRequestDTO paymentEditRequestDTO);

    BigDecimal sumPaymentsByClientIdAndCurrency(UUID clientId, InvoiceCurrency currency);

    void deletePayment(UUID paymentId);
}
