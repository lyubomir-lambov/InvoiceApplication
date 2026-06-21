package bg.softuni.invoiceapplication.service;

import bg.softuni.invoiceapplication.model.dto.PaymentCreateRequestDTO;
import bg.softuni.invoiceapplication.model.dto.PaymentEditRequestDTO;
import bg.softuni.invoiceapplication.model.entity.Payment;

import java.util.List;
import java.util.UUID;

public interface PaymentService {

    Payment createPayment(PaymentCreateRequestDTO paymentCreateRequestDTO);

    List<Payment> findAllPayments();

    List<Payment> findPaymentsByCompanyName(String companyName);

    PaymentEditRequestDTO getPaymentForEdit(UUID paymentId);

    void editPayment(PaymentEditRequestDTO paymentEditRequestDTO);

    void deletePayment(UUID paymentId);
}
