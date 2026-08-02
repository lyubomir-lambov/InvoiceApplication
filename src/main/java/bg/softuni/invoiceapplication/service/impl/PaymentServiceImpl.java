package bg.softuni.invoiceapplication.service.impl;

import bg.softuni.invoiceapplication.exception.ResourceNotFoundException;
import bg.softuni.invoiceapplication.model.dto.payments.PaymentCreateRequestDTO;
import bg.softuni.invoiceapplication.model.dto.payments.PaymentEditRequestDTO;
import bg.softuni.invoiceapplication.model.entity.Client;
import bg.softuni.invoiceapplication.model.entity.Payment;
import bg.softuni.invoiceapplication.repository.ClientRepository;
import bg.softuni.invoiceapplication.repository.PaymentRepository;
import bg.softuni.invoiceapplication.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final ClientRepository clientRepository;

    @Override
    public Payment createPayment(PaymentCreateRequestDTO paymentCreateRequestDTO) {
        if (paymentCreateRequestDTO == null) {
            throw new IllegalArgumentException("Payment create request must not be null");
        }

        Client client = clientRepository.findById(paymentCreateRequestDTO.getClientId())
                .orElseThrow(() -> new ResourceNotFoundException("Client with id " + paymentCreateRequestDTO.getClientId() + " does not exist"));

        Payment payment = Payment.builder()
                .client(client)
                .amount(paymentCreateRequestDTO.getAmount())
                .currency(paymentCreateRequestDTO.getCurrency())
                .paymentDate(paymentCreateRequestDTO.getPaymentDate())
                .notes(paymentCreateRequestDTO.getNotes())
                .build();

        Payment savedPayment = paymentRepository.save(payment);
        log.info("Payment created: paymentId={}, clientId={}, amount={}, currency={}",
                savedPayment.getId(),
                client.getId(),
                savedPayment.getAmount(),
                savedPayment.getCurrency());
        return savedPayment;
    }

    @Override
    public List<Payment> findAllPayments() {
        return paymentRepository.findAll(Sort.by(Sort.Order.desc("paymentDate")));
    }

    @Override
    public List<Payment> findPaymentsByCompanyName(String companyName) {
        if (companyName == null || companyName.isBlank()) {
            return findAllPayments();
        }

        return paymentRepository.findPaymentsByCompanyName(companyName.trim());
    }

    @Override
    public PaymentEditRequestDTO getPaymentForEdit(UUID paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment with id " + paymentId + " does not exist"));

        return PaymentEditRequestDTO.builder()
                .id(payment.getId())
                .clientId(payment.getClient().getId())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .paymentDate(payment.getPaymentDate())
                .notes(payment.getNotes())
                .build();
    }

    @Override
    public void editPayment(PaymentEditRequestDTO paymentEditRequestDTO) {
        if (paymentEditRequestDTO == null) {
            throw new IllegalArgumentException("Payment edit request must not be null");
        }

        Payment payment = paymentRepository.findById(paymentEditRequestDTO.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Payment with id " + paymentEditRequestDTO.getId() + " does not exist"));

        Client client = clientRepository.findById(paymentEditRequestDTO.getClientId())
                .orElseThrow(() -> new ResourceNotFoundException("Client with id " + paymentEditRequestDTO.getClientId() + " does not exist"));

        payment.setClient(client);
        payment.setAmount(paymentEditRequestDTO.getAmount());
        payment.setCurrency(paymentEditRequestDTO.getCurrency());
        payment.setPaymentDate(paymentEditRequestDTO.getPaymentDate());
        payment.setNotes(paymentEditRequestDTO.getNotes());

        paymentRepository.save(payment);
        log.info("Payment edited: paymentId={}, clientId={}, amount={}, currency={}",
                payment.getId(),
                client.getId(),
                payment.getAmount(),
                payment.getCurrency());
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public void deletePayment(UUID paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment with id " + paymentId + " does not exist"));

        paymentRepository.delete(payment);
        log.info("Payment deleted: paymentId={}", payment.getId());
    }
}
