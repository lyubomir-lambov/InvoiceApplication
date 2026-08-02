package bg.softuni.invoiceapplication.service.impl;

import bg.softuni.invoiceapplication.exception.ResourceNotFoundException;
import bg.softuni.invoiceapplication.model.dto.payments.PaymentCreateRequestDTO;
import bg.softuni.invoiceapplication.model.dto.payments.PaymentEditRequestDTO;
import bg.softuni.invoiceapplication.model.entity.Client;
import bg.softuni.invoiceapplication.model.entity.Payment;
import bg.softuni.invoiceapplication.model.enums.Country;
import bg.softuni.invoiceapplication.model.enums.InvoiceCurrency;
import bg.softuni.invoiceapplication.repository.ClientRepository;
import bg.softuni.invoiceapplication.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PaymentServiceImplTest {

    private static final UUID CLIENT_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID SECOND_CLIENT_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");
    private static final UUID PAYMENT_ID = UUID.fromString("44444444-4444-4444-4444-444444444444");

    private FakePaymentRepository fakePaymentRepository;
    private FakeClientRepository fakeClientRepository;
    private PaymentServiceImpl paymentService;

    @BeforeEach
    void setUp() {
        fakePaymentRepository = new FakePaymentRepository();
        fakeClientRepository = new FakeClientRepository();

        paymentService = new PaymentServiceImpl(
                fakePaymentRepository.repository(),
                fakeClientRepository.repository()
        );
    }

    @Test
    void createPayment_shouldSavePayment_whenRequestIsValid() {
        Client client = createClient(CLIENT_ID, "Lambi");
        fakeClientRepository.addClient(client);
        PaymentCreateRequestDTO requestDTO = createPaymentCreateRequestDTO();

        Payment result = paymentService.createPayment(requestDTO);

        assertThat(result.getClient()).isSameAs(client);
        assertThat(result.getAmount()).isEqualByComparingTo("150.00");
        assertThat(result.getCurrency()).isEqualTo(InvoiceCurrency.BGN);
        assertThat(result.getPaymentDate()).isEqualTo(LocalDate.of(2026, 8, 2));
        assertThat(result.getNotes()).isEqualTo("Paid by bank transfer");
        assertThat(fakePaymentRepository.savedPayment).isSameAs(result);
    }

    @Test
    void createPayment_shouldThrowException_whenRequestIsNull() {
        assertThatThrownBy(() -> paymentService.createPayment(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Payment create request must not be null");

        assertThat(fakePaymentRepository.savedPayment).isNull();
    }

    @Test
    void createPayment_shouldThrowException_whenClientDoesNotExist() {
        PaymentCreateRequestDTO requestDTO = createPaymentCreateRequestDTO();

        assertThatThrownBy(() -> paymentService.createPayment(requestDTO))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Client with id " + CLIENT_ID + " does not exist");

        assertThat(fakePaymentRepository.savedPayment).isNull();
    }

    @Test
    void findAllPayments_shouldReturnPaymentsOrderedByPaymentDateDescending() {
        Payment olderPayment = createPayment(PAYMENT_ID, createClient(CLIENT_ID, "Lambi"), "50.00", LocalDate.of(2026, 8, 1));
        Payment newerPayment = createPayment(UUID.randomUUID(), createClient(SECOND_CLIENT_ID, "Other"), "100.00", LocalDate.of(2026, 8, 3));
        fakePaymentRepository.addPayment(olderPayment);
        fakePaymentRepository.addPayment(newerPayment);

        List<Payment> result = paymentService.findAllPayments();

        assertThat(result).containsExactly(newerPayment, olderPayment);
        assertThat(fakePaymentRepository.findAllCalled).isTrue();
    }

    @Test
    void findPaymentsByCompanyName_shouldReturnAllPayments_whenCompanyNameIsBlank() {
        Payment payment = createPayment(PAYMENT_ID, createClient(CLIENT_ID, "Lambi"), "50.00", LocalDate.of(2026, 8, 1));
        fakePaymentRepository.addPayment(payment);

        List<Payment> result = paymentService.findPaymentsByCompanyName("   ");

        assertThat(result).containsExactly(payment);
        assertThat(fakePaymentRepository.findAllCalled).isTrue();
    }

    @Test
    void findPaymentsByCompanyName_shouldSearchByTrimmedCompanyName_whenCompanyNameIsPresent() {
        Payment payment = createPayment(PAYMENT_ID, createClient(CLIENT_ID, "Lambi"), "50.00", LocalDate.of(2026, 8, 1));
        fakePaymentRepository.searchResult = List.of(payment);

        List<Payment> result = paymentService.findPaymentsByCompanyName("  Lam  ");

        assertThat(result).containsExactly(payment);
        assertThat(fakePaymentRepository.lastSearchedCompanyName).isEqualTo("Lam");
    }

    @Test
    void getPaymentForEdit_shouldReturnEditDTO_whenPaymentExists() {
        Client client = createClient(CLIENT_ID, "Lambi");
        Payment payment = createPayment(PAYMENT_ID, client, "150.00", LocalDate.of(2026, 8, 2));
        fakePaymentRepository.addPayment(payment);

        PaymentEditRequestDTO result = paymentService.getPaymentForEdit(PAYMENT_ID);

        assertThat(result.getId()).isEqualTo(PAYMENT_ID);
        assertThat(result.getClientId()).isEqualTo(CLIENT_ID);
        assertThat(result.getAmount()).isEqualByComparingTo("150.00");
        assertThat(result.getCurrency()).isEqualTo(InvoiceCurrency.BGN);
        assertThat(result.getPaymentDate()).isEqualTo(LocalDate.of(2026, 8, 2));
        assertThat(result.getNotes()).isEqualTo("Paid by bank transfer");
    }

    @Test
    void getPaymentForEdit_shouldThrowException_whenPaymentDoesNotExist() {
        assertThatThrownBy(() -> paymentService.getPaymentForEdit(PAYMENT_ID))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Payment with id " + PAYMENT_ID + " does not exist");
    }

    @Test
    void editPayment_shouldUpdateAndSavePayment_whenRequestIsValid() {
        Client oldClient = createClient(CLIENT_ID, "Lambi");
        Client newClient = createClient(SECOND_CLIENT_ID, "Other");
        Payment payment = createPayment(PAYMENT_ID, oldClient, "50.00", LocalDate.of(2026, 8, 1));
        fakePaymentRepository.addPayment(payment);
        fakeClientRepository.addClient(newClient);
        PaymentEditRequestDTO requestDTO = createPaymentEditRequestDTO();

        paymentService.editPayment(requestDTO);

        assertThat(payment.getClient()).isSameAs(newClient);
        assertThat(payment.getAmount()).isEqualByComparingTo("200.00");
        assertThat(payment.getCurrency()).isEqualTo(InvoiceCurrency.EUR);
        assertThat(payment.getPaymentDate()).isEqualTo(LocalDate.of(2026, 8, 4));
        assertThat(payment.getNotes()).isEqualTo("Updated notes");
        assertThat(fakePaymentRepository.savedPayment).isSameAs(payment);
    }

    @Test
    void editPayment_shouldThrowException_whenRequestIsNull() {
        assertThatThrownBy(() -> paymentService.editPayment(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Payment edit request must not be null");

        assertThat(fakePaymentRepository.savedPayment).isNull();
    }

    @Test
    void editPayment_shouldThrowException_whenPaymentDoesNotExist() {
        PaymentEditRequestDTO requestDTO = createPaymentEditRequestDTO();

        assertThatThrownBy(() -> paymentService.editPayment(requestDTO))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Payment with id " + PAYMENT_ID + " does not exist");

        assertThat(fakePaymentRepository.savedPayment).isNull();
    }

    @Test
    void editPayment_shouldThrowException_whenClientDoesNotExist() {
        Payment payment = createPayment(PAYMENT_ID, createClient(CLIENT_ID, "Lambi"), "50.00", LocalDate.of(2026, 8, 1));
        fakePaymentRepository.addPayment(payment);
        PaymentEditRequestDTO requestDTO = createPaymentEditRequestDTO();

        assertThatThrownBy(() -> paymentService.editPayment(requestDTO))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Client with id " + SECOND_CLIENT_ID + " does not exist");

        assertThat(fakePaymentRepository.savedPayment).isNull();
    }

    @Test
    void deletePayment_shouldDeletePayment_whenPaymentExists() {
        Payment payment = createPayment(PAYMENT_ID, createClient(CLIENT_ID, "Lambi"), "50.00", LocalDate.of(2026, 8, 1));
        fakePaymentRepository.addPayment(payment);

        paymentService.deletePayment(PAYMENT_ID);

        assertThat(fakePaymentRepository.deletedPayment).isSameAs(payment);
    }

    @Test
    void deletePayment_shouldThrowException_whenPaymentDoesNotExist() {
        assertThatThrownBy(() -> paymentService.deletePayment(PAYMENT_ID))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Payment with id " + PAYMENT_ID + " does not exist");

        assertThat(fakePaymentRepository.deletedPayment).isNull();
    }

    private PaymentCreateRequestDTO createPaymentCreateRequestDTO() {
        return PaymentCreateRequestDTO.builder()
                .clientId(CLIENT_ID)
                .amount(new BigDecimal("150.00"))
                .currency(InvoiceCurrency.BGN)
                .paymentDate(LocalDate.of(2026, 8, 2))
                .notes("Paid by bank transfer")
                .build();
    }

    private PaymentEditRequestDTO createPaymentEditRequestDTO() {
        return PaymentEditRequestDTO.builder()
                .id(PAYMENT_ID)
                .clientId(SECOND_CLIENT_ID)
                .amount(new BigDecimal("200.00"))
                .currency(InvoiceCurrency.EUR)
                .paymentDate(LocalDate.of(2026, 8, 4))
                .notes("Updated notes")
                .build();
    }

    private Client createClient(UUID id, String displayName) {
        Client client = Client.builder()
                .displayName(displayName)
                .companyName(displayName + " Ltd.")
                .country(Country.BULGARIA)
                .vatRegistered(true)
                .active(true)
                .build();
        ReflectionTestUtils.setField(client, "id", id);
        return client;
    }

    private Payment createPayment(UUID id, Client client, String amount, LocalDate paymentDate) {
        Payment payment = Payment.builder()
                .client(client)
                .amount(new BigDecimal(amount))
                .currency(InvoiceCurrency.BGN)
                .paymentDate(paymentDate)
                .notes("Paid by bank transfer")
                .build();
        ReflectionTestUtils.setField(payment, "id", id);
        return payment;
    }

    private static final class FakePaymentRepository {

        private final Map<UUID, Payment> payments = new LinkedHashMap<>();

        private boolean findAllCalled;
        private String lastSearchedCompanyName;
        private List<Payment> searchResult = List.of();
        private Payment savedPayment;
        private Payment deletedPayment;

        private void addPayment(Payment payment) {
            payments.put(payment.getId(), payment);
        }

        private PaymentRepository repository() {
            return proxy(PaymentRepository.class, (proxy, method, args) -> switch (method.getName()) {
                case "findAll" -> {
                    findAllCalled = true;
                    yield payments.values()
                            .stream()
                            .sorted(Comparator.comparing(Payment::getPaymentDate).reversed())
                            .toList();
                }
                case "findPaymentsByCompanyName" -> {
                    lastSearchedCompanyName = (String) args[0];
                    yield searchResult;
                }
                case "findById" -> Optional.ofNullable(payments.get((UUID) args[0]));
                case "save" -> {
                    savedPayment = (Payment) args[0];
                    yield savedPayment;
                }
                case "delete" -> {
                    deletedPayment = (Payment) args[0];
                    yield null;
                }
                default -> defaultValue(method.getReturnType());
            });
        }
    }

    private static final class FakeClientRepository {

        private final Map<UUID, Client> clients = new LinkedHashMap<>();

        private void addClient(Client client) {
            clients.put(client.getId(), client);
        }

        private ClientRepository repository() {
            return proxy(ClientRepository.class, (proxy, method, args) -> switch (method.getName()) {
                case "findById" -> Optional.ofNullable(clients.get((UUID) args[0]));
                default -> defaultValue(method.getReturnType());
            });
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> T proxy(Class<T> type, InvocationHandler invocationHandler) {
        return (T) Proxy.newProxyInstance(
                type.getClassLoader(),
                new Class<?>[]{type},
                invocationHandler
        );
    }

    private static Object defaultValue(Class<?> returnType) {
        if (returnType.equals(boolean.class)) {
            return false;
        }

        if (returnType.equals(Optional.class)) {
            return Optional.empty();
        }

        if (returnType.equals(List.class)) {
            return new ArrayList<>();
        }

        return null;
    }
}
