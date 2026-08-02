package bg.softuni.invoiceapplication.service.impl;

import bg.softuni.invoiceapplication.model.dto.reports.PaymentReportByCurrencyDTO;
import bg.softuni.invoiceapplication.model.dto.reports.PaymentReportClientDTO;
import bg.softuni.invoiceapplication.model.entity.Client;
import bg.softuni.invoiceapplication.model.entity.Invoice;
import bg.softuni.invoiceapplication.model.entity.InvoiceLineItem;
import bg.softuni.invoiceapplication.model.entity.Payment;
import bg.softuni.invoiceapplication.model.enums.Country;
import bg.softuni.invoiceapplication.model.enums.InvoiceCurrency;
import bg.softuni.invoiceapplication.model.enums.InvoiceStatus;
import bg.softuni.invoiceapplication.model.enums.InvoiceType;
import bg.softuni.invoiceapplication.model.enums.MeasurementUnit;
import bg.softuni.invoiceapplication.model.enums.VatRate;
import bg.softuni.invoiceapplication.repository.ClientRepository;
import bg.softuni.invoiceapplication.repository.InvoiceRepository;
import bg.softuni.invoiceapplication.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PaymentReportServiceImplTest {

    private static final UUID CLIENT_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID SECOND_CLIENT_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");

    private FakeClientRepository fakeClientRepository;
    private FakeInvoiceRepository fakeInvoiceRepository;
    private FakePaymentRepository fakePaymentRepository;
    private PaymentReportServiceImpl paymentReportService;

    @BeforeEach
    void setUp() {
        fakeClientRepository = new FakeClientRepository();
        fakeInvoiceRepository = new FakeInvoiceRepository();
        fakePaymentRepository = new FakePaymentRepository();

        paymentReportService = new PaymentReportServiceImpl(
                fakeClientRepository.repository(),
                fakeInvoiceRepository.repository(),
                fakePaymentRepository.repository()
        );
    }

    @Test
    void getReportsByCurrency_shouldCalculateInvoicePaymentAndDueTotalsByCurrency() {
        Client client = createClient(CLIENT_ID, "Lambi");
        fakeInvoiceRepository.addInvoice(createInvoice(UUID.randomUUID(), client, InvoiceType.INVOICE, InvoiceStatus.ISSUED, InvoiceCurrency.BGN, "100.00"));
        fakeInvoiceRepository.addInvoice(createInvoice(UUID.randomUUID(), client, InvoiceType.CREDIT_NOTE, InvoiceStatus.ISSUED, InvoiceCurrency.BGN, "20.00"));
        fakeInvoiceRepository.addInvoice(createInvoice(UUID.randomUUID(), client, InvoiceType.INVOICE, InvoiceStatus.CANCELLED, InvoiceCurrency.BGN, "300.00"));
        fakeInvoiceRepository.addInvoice(createInvoice(UUID.randomUUID(), client, InvoiceType.INVOICE, InvoiceStatus.ISSUED, InvoiceCurrency.EUR, "50.00"));
        fakePaymentRepository.addPayment(createPayment(UUID.randomUUID(), client, InvoiceCurrency.BGN, "30.00"));
        fakePaymentRepository.addPayment(createPayment(UUID.randomUUID(), client, InvoiceCurrency.EUR, "10.00"));

        List<PaymentReportByCurrencyDTO> result = paymentReportService.getReportsByCurrency(null);

        assertThat(result).hasSize(2);

        PaymentReportByCurrencyDTO bgnReport = findCurrencyReport(result, InvoiceCurrency.BGN);
        assertThat(bgnReport.getInvoiceTotal()).isEqualByComparingTo("80.00");
        assertThat(bgnReport.getPaymentTotal()).isEqualByComparingTo("30.00");
        assertThat(bgnReport.getDueAmount()).isEqualByComparingTo("50.00");

        PaymentReportByCurrencyDTO eurReport = findCurrencyReport(result, InvoiceCurrency.EUR);
        assertThat(eurReport.getInvoiceTotal()).isEqualByComparingTo("50.00");
        assertThat(eurReport.getPaymentTotal()).isEqualByComparingTo("10.00");
        assertThat(eurReport.getDueAmount()).isEqualByComparingTo("40.00");
    }

    @Test
    void getReportsByCurrency_shouldFilterByClient_whenClientIdIsProvided() {
        Client client = createClient(CLIENT_ID, "Lambi");
        Client secondClient = createClient(SECOND_CLIENT_ID, "Other");
        fakeInvoiceRepository.addInvoice(createInvoice(UUID.randomUUID(), client, InvoiceType.INVOICE, InvoiceStatus.ISSUED, InvoiceCurrency.BGN, "100.00"));
        fakeInvoiceRepository.addInvoice(createInvoice(UUID.randomUUID(), secondClient, InvoiceType.INVOICE, InvoiceStatus.ISSUED, InvoiceCurrency.BGN, "999.00"));
        fakePaymentRepository.addPayment(createPayment(UUID.randomUUID(), client, InvoiceCurrency.BGN, "40.00"));
        fakePaymentRepository.addPayment(createPayment(UUID.randomUUID(), secondClient, InvoiceCurrency.BGN, "999.00"));

        List<PaymentReportByCurrencyDTO> result = paymentReportService.getReportsByCurrency(CLIENT_ID);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCurrency()).isEqualTo(InvoiceCurrency.BGN);
        assertThat(result.get(0).getInvoiceTotal()).isEqualByComparingTo("100.00");
        assertThat(result.get(0).getPaymentTotal()).isEqualByComparingTo("40.00");
        assertThat(result.get(0).getDueAmount()).isEqualByComparingTo("60.00");
    }

    @Test
    void getClientReports_shouldReturnOnlyClientsWithReportData_whenClientIdIsNull() {
        Client client = createClient(CLIENT_ID, "Lambi");
        Client emptyClient = createClient(SECOND_CLIENT_ID, "Empty");
        fakeClientRepository.addClient(client);
        fakeClientRepository.addClient(emptyClient);
        fakeInvoiceRepository.addInvoice(createInvoice(UUID.randomUUID(), client, InvoiceType.INVOICE, InvoiceStatus.ISSUED, InvoiceCurrency.BGN, "120.00"));
        fakePaymentRepository.addPayment(createPayment(UUID.randomUUID(), client, InvoiceCurrency.BGN, "50.00"));

        List<PaymentReportClientDTO> result = paymentReportService.getClientReports(null);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getClientId()).isEqualTo(CLIENT_ID);
        assertThat(result.get(0).getInvoiceCurrencyGroups()).hasSize(1);
        assertThat(result.get(0).getInvoiceCurrencyGroups().get(0).getTotalAmount()).isEqualByComparingTo("120.00");
        assertThat(result.get(0).getPaymentCurrencyGroups()).hasSize(1);
        assertThat(result.get(0).getPaymentCurrencyGroups().get(0).getTotalAmount()).isEqualByComparingTo("50.00");
    }

    @Test
    void getClientReports_shouldReturnSelectedClientEvenWithoutReportData_whenClientIdIsProvided() {
        Client client = createClient(CLIENT_ID, "Lambi");
        fakeClientRepository.addClient(client);

        List<PaymentReportClientDTO> result = paymentReportService.getClientReports(CLIENT_ID);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getClientId()).isEqualTo(CLIENT_ID);
        assertThat(result.get(0).getInvoiceCurrencyGroups()).isEmpty();
        assertThat(result.get(0).getPaymentCurrencyGroups()).isEmpty();
    }

    @Test
    void getClientReports_shouldReturnEmptyList_whenSelectedClientDoesNotExist() {
        List<PaymentReportClientDTO> result = paymentReportService.getClientReports(CLIENT_ID);

        assertThat(result).isEmpty();
    }

    @Test
    void getClientReports_shouldGroupInvoicesAndPaymentsByCurrency() {
        Client client = createClient(CLIENT_ID, "Lambi");
        fakeClientRepository.addClient(client);
        fakeInvoiceRepository.addInvoice(createInvoice(UUID.randomUUID(), client, InvoiceType.INVOICE, InvoiceStatus.ISSUED, InvoiceCurrency.BGN, "100.00"));
        fakeInvoiceRepository.addInvoice(createInvoice(UUID.randomUUID(), client, InvoiceType.INVOICE, InvoiceStatus.ISSUED, InvoiceCurrency.EUR, "50.00"));
        fakePaymentRepository.addPayment(createPayment(UUID.randomUUID(), client, InvoiceCurrency.BGN, "20.00"));
        fakePaymentRepository.addPayment(createPayment(UUID.randomUUID(), client, InvoiceCurrency.EUR, "10.00"));

        List<PaymentReportClientDTO> result = paymentReportService.getClientReports(CLIENT_ID);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getInvoiceCurrencyGroups()).hasSize(2);
        assertThat(result.get(0).getPaymentCurrencyGroups()).hasSize(2);
        assertThat(result.get(0).getInvoiceCurrencyGroups().get(0).getCurrency()).isEqualTo(InvoiceCurrency.BGN);
        assertThat(result.get(0).getPaymentCurrencyGroups().get(1).getCurrency()).isEqualTo(InvoiceCurrency.EUR);
    }

    private PaymentReportByCurrencyDTO findCurrencyReport(List<PaymentReportByCurrencyDTO> reports,
                                                          InvoiceCurrency currency) {
        return reports.stream()
                .filter(report -> report.getCurrency() == currency)
                .findFirst()
                .orElseThrow();
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

    private Invoice createInvoice(UUID id,
                                  Client client,
                                  InvoiceType invoiceType,
                                  InvoiceStatus status,
                                  InvoiceCurrency currency,
                                  String amount) {
        Invoice invoice = Invoice.builder()
                .invoiceType(invoiceType)
                .invoiceSequence(1L)
                .invoiceNumber("0000000001")
                .currency(currency)
                .status(status)
                .issueDate(LocalDate.of(2026, 8, 2))
                .dueDate(LocalDate.of(2026, 8, 16))
                .client(client)
                .clientDisplayName(client.getDisplayName())
                .clientCompanyName(client.getCompanyName())
                .clientCountry(client.getCountry())
                .build();
        ReflectionTestUtils.setField(invoice, "id", id);
        invoice.addLineItem(InvoiceLineItem.builder()
                .description("Service")
                .quantity(BigDecimal.ONE)
                .measurementUnit(MeasurementUnit.SERVICE)
                .unitPrice(new BigDecimal(amount))
                .vatRate(VatRate.ZERO)
                .build());
        return invoice;
    }

    private Payment createPayment(UUID id, Client client, InvoiceCurrency currency, String amount) {
        Payment payment = Payment.builder()
                .client(client)
                .amount(new BigDecimal(amount))
                .currency(currency)
                .paymentDate(LocalDate.of(2026, 8, 2))
                .notes("Payment")
                .build();
        ReflectionTestUtils.setField(payment, "id", id);
        return payment;
    }

    private static final class FakeClientRepository {

        private final Map<UUID, Client> clients = new LinkedHashMap<>();

        private void addClient(Client client) {
            clients.put(client.getId(), client);
        }

        private ClientRepository repository() {
            return proxy(ClientRepository.class, (proxy, method, args) -> switch (method.getName()) {
                case "findById" -> Optional.ofNullable(clients.get((UUID) args[0]));
                case "findAll" -> clients.values()
                        .stream()
                        .sorted(Comparator.comparing(Client::getDisplayName))
                        .toList();
                default -> defaultValue(method.getReturnType());
            });
        }
    }

    private static final class FakeInvoiceRepository {

        private final List<Invoice> invoices = new java.util.ArrayList<>();

        private void addInvoice(Invoice invoice) {
            invoices.add(invoice);
        }

        private InvoiceRepository repository() {
            return proxy(InvoiceRepository.class, (proxy, method, args) -> switch (method.getName()) {
                case "findAll" -> List.copyOf(invoices);
                default -> defaultValue(method.getReturnType());
            });
        }
    }

    private static final class FakePaymentRepository {

        private final List<Payment> payments = new java.util.ArrayList<>();

        private void addPayment(Payment payment) {
            payments.add(payment);
        }

        private PaymentRepository repository() {
            return proxy(PaymentRepository.class, (proxy, method, args) -> switch (method.getName()) {
                case "findAll" -> List.copyOf(payments);
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
        if (returnType.equals(Optional.class)) {
            return Optional.empty();
        }

        if (returnType.equals(List.class)) {
            return List.of();
        }

        return null;
    }
}
