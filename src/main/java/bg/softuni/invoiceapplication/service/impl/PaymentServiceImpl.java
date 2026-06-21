package bg.softuni.invoiceapplication.service.impl;

import bg.softuni.invoiceapplication.model.dto.PaymentCreateRequestDTO;
import bg.softuni.invoiceapplication.model.dto.PaymentEditRequestDTO;
import bg.softuni.invoiceapplication.model.entity.Client;
import bg.softuni.invoiceapplication.model.entity.Payment;
import bg.softuni.invoiceapplication.model.enums.InvoiceCurrency;
import bg.softuni.invoiceapplication.repository.ClientRepository;
import bg.softuni.invoiceapplication.repository.PaymentRepository;
import bg.softuni.invoiceapplication.service.PaymentService;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final ClientRepository clientRepository;

    public PaymentServiceImpl(PaymentRepository paymentRepository, ClientRepository clientRepository) {
        this.paymentRepository = paymentRepository;
        this.clientRepository = clientRepository;
    }

    @Override
    public Payment createPayment(Payment payment) {
        if (payment == null) {
            throw new IllegalArgumentException("Payment must not be null");
        }

        if (payment.getClient() == null || payment.getClient().getId() == null) {
            throw new IllegalArgumentException("Payment client must not be null");
        }

        Client client = clientRepository.findById(payment.getClient().getId())
                .orElseThrow(() -> new RuntimeException("Client with id " + payment.getClient().getId() + " does not exist"));

        payment.setClient(client);

        return paymentRepository.save(payment);
    }

    @Override
    public Payment createPayment(PaymentCreateRequestDTO paymentCreateRequestDTO) {
        if (paymentCreateRequestDTO == null) {
            throw new IllegalArgumentException("Payment create request must not be null");
        }

        Client client = clientRepository.findById(paymentCreateRequestDTO.getClientId())
                .orElseThrow(() -> new RuntimeException("Client with id " + paymentCreateRequestDTO.getClientId() + " does not exist"));

        Payment payment = Payment.builder()
                .client(client)
                .amount(paymentCreateRequestDTO.getAmount())
                .currency(paymentCreateRequestDTO.getCurrency())
                .paymentDate(paymentCreateRequestDTO.getPaymentDate())
                .notes(paymentCreateRequestDTO.getNotes())
                .build();

        return paymentRepository.save(payment);
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
    public List<Payment> findPaymentsByClientId(UUID clientId) {
        return paymentRepository.findAllByClientIdOrderByPaymentDateDesc(clientId);
    }

    @Override
    public PaymentEditRequestDTO getPaymentForEdit(UUID paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment with id " + paymentId + " does not exist"));

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
                .orElseThrow(() -> new RuntimeException("Payment with id " + paymentEditRequestDTO.getId() + " does not exist"));

        Client client = clientRepository.findById(paymentEditRequestDTO.getClientId())
                .orElseThrow(() -> new RuntimeException("Client with id " + paymentEditRequestDTO.getClientId() + " does not exist"));

        payment.setClient(client);
        payment.setAmount(paymentEditRequestDTO.getAmount());
        payment.setCurrency(paymentEditRequestDTO.getCurrency());
        payment.setPaymentDate(paymentEditRequestDTO.getPaymentDate());
        payment.setNotes(paymentEditRequestDTO.getNotes());

        paymentRepository.save(payment);
    }

    @Override
    public BigDecimal sumPaymentsByClientIdAndCurrency(UUID clientId, InvoiceCurrency currency) {
        BigDecimal totalPayments = paymentRepository.sumPaymentsByClientIdAndCurrency(clientId, currency);

        return totalPayments == null ? BigDecimal.ZERO : totalPayments;
    }

    @Override
    public void deletePayment(UUID paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment with id " + paymentId + " does not exist"));

        paymentRepository.delete(payment);
    }
}
