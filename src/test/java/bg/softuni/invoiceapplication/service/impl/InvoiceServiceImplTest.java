package bg.softuni.invoiceapplication.service.impl;

import bg.softuni.invoiceapplication.exception.BusinessRuleException;
import bg.softuni.invoiceapplication.exception.ResourceNotFoundException;
import bg.softuni.invoiceapplication.mapper.invoice.InvoiceMapper;
import bg.softuni.invoiceapplication.mapper.invoicehistory.InvoiceHistoryMapper;
import bg.softuni.invoiceapplication.model.dto.invoicehistory.InvoiceHistoryCreateRequestDTO;
import bg.softuni.invoiceapplication.model.dto.invoicehistory.InvoiceHistoryResponseDTO;
import bg.softuni.invoiceapplication.model.dto.invoices.InvoiceCreateRequestDTO;
import bg.softuni.invoiceapplication.model.dto.invoices.InvoiceDetailsDTO;
import bg.softuni.invoiceapplication.model.dto.invoices.InvoiceEditRequestDTO;
import bg.softuni.invoiceapplication.model.dto.invoices.InvoiceLineItemCreateRequestDTO;
import bg.softuni.invoiceapplication.model.dto.invoices.InvoiceShowAllDTO;
import bg.softuni.invoiceapplication.model.entity.Client;
import bg.softuni.invoiceapplication.model.entity.Invoice;
import bg.softuni.invoiceapplication.model.entity.InvoiceLineItem;
import bg.softuni.invoiceapplication.model.enums.Country;
import bg.softuni.invoiceapplication.model.enums.InvoiceCurrency;
import bg.softuni.invoiceapplication.model.enums.InvoiceStatus;
import bg.softuni.invoiceapplication.model.enums.InvoiceType;
import bg.softuni.invoiceapplication.model.enums.MeasurementUnit;
import bg.softuni.invoiceapplication.model.enums.VatRate;
import bg.softuni.invoiceapplication.repository.ClientRepository;
import bg.softuni.invoiceapplication.repository.InvoiceRepository;
import bg.softuni.invoiceapplication.service.InvoiceHistoryIntegrationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
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

class InvoiceServiceImplTest {

    private static final UUID CLIENT_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID SECOND_CLIENT_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");
    private static final UUID INVOICE_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");

    private FakeInvoiceRepository fakeInvoiceRepository;
    private FakeClientRepository fakeClientRepository;
    private FakeInvoiceHistoryIntegrationService fakeInvoiceHistoryIntegrationService;
    private InvoiceServiceImpl invoiceService;

    @BeforeEach
    void setUp() {
        fakeInvoiceRepository = new FakeInvoiceRepository();
        fakeClientRepository = new FakeClientRepository();
        fakeInvoiceHistoryIntegrationService = new FakeInvoiceHistoryIntegrationService();

        invoiceService = new InvoiceServiceImpl(
                fakeInvoiceRepository.repository(),
                fakeClientRepository.repository(),
                new InvoiceMapper(),
                new InvoiceHistoryMapper(new ObjectMapper().registerModule(new JavaTimeModule())),
                fakeInvoiceHistoryIntegrationService
        );
    }

    @Test
    void prepareCreateInvoiceForm_shouldCreateDefaults_whenNoInvoicesExist() {
        InvoiceCreateRequestDTO result = invoiceService.prepareCreateInvoiceForm();

        assertThat(result.getInvoiceType()).isEqualTo(InvoiceType.INVOICE);
        assertThat(result.getCurrency()).isEqualTo(InvoiceCurrency.BGN);
        assertThat(result.getInvoiceSequence()).isEqualTo(1L);
        assertThat(result.getInvoiceNumber()).isEqualTo("0000000001");
        assertThat(result.getIssueDate()).isEqualTo(LocalDate.now());
        assertThat(result.getDueDate()).isEqualTo(LocalDate.now().plusDays(14));
        assertThat(result.getLineItems()).hasSize(1);
    }

    @Test
    void prepareCreateInvoiceForm_shouldUseNextSequence_whenLastInvoiceExists() {
        fakeInvoiceRepository.addInvoice(createInvoice(INVOICE_ID, createClient(CLIENT_ID, true), 7L, InvoiceStatus.ISSUED));

        InvoiceCreateRequestDTO result = invoiceService.prepareCreateInvoiceForm();

        assertThat(result.getInvoiceSequence()).isEqualTo(8L);
        assertThat(result.getInvoiceNumber()).isEqualTo("0000000008");
    }

    @Test
    void findAllInvoices_shouldReturnMappedInvoicesOrderedBySequenceDesc() {
        Invoice olderInvoice = createInvoice(INVOICE_ID, createClient(CLIENT_ID, true), 1L, InvoiceStatus.ISSUED);
        Invoice newerInvoice = createInvoice(UUID.randomUUID(), createClient(SECOND_CLIENT_ID, true), 2L, InvoiceStatus.ISSUED);
        fakeInvoiceRepository.addInvoice(olderInvoice);
        fakeInvoiceRepository.addInvoice(newerInvoice);

        List<InvoiceShowAllDTO> result = invoiceService.findAllInvoices();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getInvoiceNumber()).isEqualTo("0000000002");
        assertThat(result.get(1).getInvoiceNumber()).isEqualTo("0000000001");
    }

    @Test
    void findInvoicesByCompanyName_shouldReturnAllInvoices_whenCompanyNameIsBlank() {
        fakeInvoiceRepository.addInvoice(createInvoice(INVOICE_ID, createClient(CLIENT_ID, true), 1L, InvoiceStatus.ISSUED));

        List<InvoiceShowAllDTO> result = invoiceService.findInvoicesByCompanyName("   ");

        assertThat(result).hasSize(1);
        assertThat(fakeInvoiceRepository.findAllCalled).isTrue();
    }

    @Test
    void findInvoicesByCompanyName_shouldSearchByTrimmedCompanyName_whenCompanyNameIsPresent() {
        fakeInvoiceRepository.searchResult = List.of(createInvoice(INVOICE_ID, createClient(CLIENT_ID, true), 1L, InvoiceStatus.ISSUED));

        List<InvoiceShowAllDTO> result = invoiceService.findInvoicesByCompanyName("  Lambi  ");

        assertThat(result).hasSize(1);
        assertThat(fakeInvoiceRepository.lastSearchedCompanyName).isEqualTo("Lambi");
    }

    @Test
    void findInvoiceById_shouldReturnDetailsDTO_whenInvoiceExists() {
        fakeInvoiceRepository.addInvoice(createInvoice(INVOICE_ID, createClient(CLIENT_ID, true), 1L, InvoiceStatus.ISSUED));

        InvoiceDetailsDTO result = invoiceService.findInvoiceById(INVOICE_ID);

        assertThat(result.getId()).isEqualTo(INVOICE_ID);
        assertThat(result.getInvoiceNumber()).isEqualTo("0000000001");
        assertThat(result.getTotalAmount()).isEqualByComparingTo("120.00");
    }

    @Test
    void findInvoiceById_shouldThrowException_whenInvoiceDoesNotExist() {
        assertThatThrownBy(() -> invoiceService.findInvoiceById(INVOICE_ID))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Invoice with id " + INVOICE_ID + " does not exist");
    }

    @Test
    void createInvoice_shouldSaveInvoiceAndCreateHistoryRecord_whenRequestIsValid() {
        Client client = createClient(CLIENT_ID, true);
        fakeClientRepository.addClient(client);
        fakeInvoiceRepository.addInvoice(createInvoice(UUID.randomUUID(), client, 4L, InvoiceStatus.ISSUED));
        InvoiceCreateRequestDTO requestDTO = createInvoiceCreateRequestDTO(CLIENT_ID);

        Invoice result = invoiceService.createInvoice(requestDTO, "admin");

        assertThat(result.getInvoiceSequence()).isEqualTo(5L);
        assertThat(result.getInvoiceNumber()).isEqualTo("0000000005");
        assertThat(result.getStatus()).isEqualTo(InvoiceStatus.ISSUED);
        assertThat(result.getClient()).isSameAs(client);
        assertThat(result.getClientDisplayName()).isEqualTo("Lambi");
        assertThat(result.getLineItems()).hasSize(1);
        assertThat(fakeInvoiceRepository.savedInvoice).isSameAs(result);
        assertThat(fakeInvoiceHistoryIntegrationService.createdRequests).hasSize(1);
        assertThat(fakeInvoiceHistoryIntegrationService.createdRequests.get(0).getAction()).isEqualTo("CREATED");
        assertThat(fakeInvoiceHistoryIntegrationService.createdRequests.get(0).getPerformedByUsername()).isEqualTo("admin");
    }

    @Test
    void createInvoice_shouldUseSystemUsername_whenPerformedByUsernameIsBlank() {
        Client client = createClient(CLIENT_ID, true);
        fakeClientRepository.addClient(client);
        InvoiceCreateRequestDTO requestDTO = createInvoiceCreateRequestDTO(CLIENT_ID);

        invoiceService.createInvoice(requestDTO, "   ");

        assertThat(fakeInvoiceHistoryIntegrationService.createdRequests.get(0).getPerformedByUsername()).isEqualTo("system");
    }

    @Test
    void createInvoice_shouldThrowException_whenRequestIsNull() {
        assertThatThrownBy(() -> invoiceService.createInvoice(null, "admin"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invoice create request must not be null");

        assertThat(fakeInvoiceRepository.savedInvoice).isNull();
    }

    @Test
    void createInvoice_shouldThrowException_whenClientDoesNotExist() {
        InvoiceCreateRequestDTO requestDTO = createInvoiceCreateRequestDTO(CLIENT_ID);

        assertThatThrownBy(() -> invoiceService.createInvoice(requestDTO, "admin"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Client with id " + CLIENT_ID + " does not exist");
    }

    @Test
    void createInvoice_shouldThrowException_whenClientIsInactive() {
        fakeClientRepository.addClient(createClient(CLIENT_ID, false));
        InvoiceCreateRequestDTO requestDTO = createInvoiceCreateRequestDTO(CLIENT_ID);

        assertThatThrownBy(() -> invoiceService.createInvoice(requestDTO, "admin"))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Cannot create invoice for inactive client");
    }

    @Test
    void createInvoice_shouldThrowException_whenDueDateIsBeforeIssueDate() {
        fakeClientRepository.addClient(createClient(CLIENT_ID, true));
        InvoiceCreateRequestDTO requestDTO = createInvoiceCreateRequestDTO(CLIENT_ID);
        requestDTO.setDueDate(LocalDate.of(2026, 7, 31));

        assertThatThrownBy(() -> invoiceService.createInvoice(requestDTO, "admin"))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Due date cannot be before issue date");
    }

    @Test
    void createInvoice_shouldThrowException_whenIssueDateIsBeforeLastInvoiceIssueDate() {
        Client client = createClient(CLIENT_ID, true);
        fakeClientRepository.addClient(client);
        fakeInvoiceRepository.addInvoice(createInvoice(UUID.randomUUID(), client, 4L, InvoiceStatus.ISSUED));
        InvoiceCreateRequestDTO requestDTO = createInvoiceCreateRequestDTO(CLIENT_ID);
        requestDTO.setIssueDate(LocalDate.of(2026, 7, 31));

        assertThatThrownBy(() -> invoiceService.createInvoice(requestDTO, "admin"))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Issue date cannot be before last invoice issue date");
    }

    @Test
    void getInvoiceForEdit_shouldReturnEditDTO_whenInvoiceExists() {
        fakeInvoiceRepository.addInvoice(createInvoice(INVOICE_ID, createClient(CLIENT_ID, true), 1L, InvoiceStatus.ISSUED));

        InvoiceEditRequestDTO result = invoiceService.getInvoiceForEdit(INVOICE_ID);

        assertThat(result.getId()).isEqualTo(INVOICE_ID);
        assertThat(result.getInvoiceNumber()).isEqualTo("0000000001");
        assertThat(result.getClientId()).isEqualTo(CLIENT_ID);
    }

    @Test
    void cancelInvoice_shouldSetCancelledStatusAndCreateHistoryRecord_whenInvoiceExists() {
        Invoice invoice = createInvoice(INVOICE_ID, createClient(CLIENT_ID, true), 1L, InvoiceStatus.ISSUED);
        fakeInvoiceRepository.addInvoice(invoice);

        invoiceService.cancelInvoice(INVOICE_ID, "admin");

        assertThat(invoice.getStatus()).isEqualTo(InvoiceStatus.CANCELLED);
        assertThat(fakeInvoiceHistoryIntegrationService.createdRequests.get(0).getAction()).isEqualTo("CANCELLED");
    }

    @Test
    void restoreInvoice_shouldSetIssuedStatusAndCreateHistoryRecord_whenInvoiceExists() {
        Invoice invoice = createInvoice(INVOICE_ID, createClient(CLIENT_ID, true), 1L, InvoiceStatus.CANCELLED);
        fakeInvoiceRepository.addInvoice(invoice);

        invoiceService.restoreInvoice(INVOICE_ID, "admin");

        assertThat(invoice.getStatus()).isEqualTo(InvoiceStatus.ISSUED);
        assertThat(fakeInvoiceHistoryIntegrationService.createdRequests.get(0).getAction()).isEqualTo("RESTORED");
    }

    @Test
    void markOverdueInvoices_shouldMarkIssuedPastDueInvoicesAndCreateHistoryRecords() {
        Invoice overdueInvoice = createInvoice(INVOICE_ID, createClient(CLIENT_ID, true), 1L, InvoiceStatus.ISSUED);
        overdueInvoice.setDueDate(LocalDate.now().minusDays(1));
        fakeInvoiceRepository.addInvoice(overdueInvoice);

        int result = invoiceService.markOverdueInvoices();

        assertThat(result).isEqualTo(1);
        assertThat(overdueInvoice.getStatus()).isEqualTo(InvoiceStatus.OVERDUE);
        assertThat(fakeInvoiceRepository.flushCount).isEqualTo(1);
        assertThat(fakeInvoiceHistoryIntegrationService.createdRequests.get(0).getAction()).isEqualTo("MARKED_OVERDUE");
        assertThat(fakeInvoiceHistoryIntegrationService.createdRequests.get(0).getPerformedByUsername()).isEqualTo("system");
    }

    @Test
    void markNoLongerOverdueInvoices_shouldMarkFutureOverdueInvoicesAsIssuedAndCreateHistoryRecords() {
        Invoice invoice = createInvoice(INVOICE_ID, createClient(CLIENT_ID, true), 1L, InvoiceStatus.OVERDUE);
        invoice.setDueDate(LocalDate.now().plusDays(1));
        fakeInvoiceRepository.addInvoice(invoice);

        int result = invoiceService.markNoLongerOverdueInvoices();

        assertThat(result).isEqualTo(1);
        assertThat(invoice.getStatus()).isEqualTo(InvoiceStatus.ISSUED);
        assertThat(fakeInvoiceRepository.flushCount).isEqualTo(1);
        assertThat(fakeInvoiceHistoryIntegrationService.createdRequests.get(0).getAction()).isEqualTo("MARKED_ISSUED");
        assertThat(fakeInvoiceHistoryIntegrationService.createdRequests.get(0).getPerformedByUsername()).isEqualTo("system");
    }

    private InvoiceCreateRequestDTO createInvoiceCreateRequestDTO(UUID clientId) {
        return InvoiceCreateRequestDTO.builder()
                .invoiceType(InvoiceType.INVOICE)
                .invoiceSequence(1L)
                .invoiceNumber("0000000001")
                .currency(InvoiceCurrency.BGN)
                .issueDate(LocalDate.of(2026, 8, 1))
                .dueDate(LocalDate.of(2026, 8, 15))
                .clientId(clientId)
                .lineItems(List.of(createLineItemRequestDTO()))
                .build();
    }

    private InvoiceLineItemCreateRequestDTO createLineItemRequestDTO() {
        return InvoiceLineItemCreateRequestDTO.builder()
                .description("Consulting")
                .quantity(new BigDecimal("2.00"))
                .measurementUnit(MeasurementUnit.HOUR)
                .unitPrice(new BigDecimal("50.00"))
                .vatRate(VatRate.TWENTY)
                .build();
    }

    private Client createClient(UUID id, boolean active) {
        Client client = Client.builder()
                .displayName("Lambi")
                .companyName("Lambi Ltd.")
                .legalRepresentative("Lyubomir Lambov")
                .email("client@example.com")
                .phoneNumber("+359888123456")
                .country(Country.BULGARIA)
                .address("Sofia")
                .vatRegistered(true)
                .vatNumber("BG123456789")
                .active(active)
                .build();
        ReflectionTestUtils.setField(client, "id", id);
        return client;
    }

    private Invoice createInvoice(UUID id, Client client, Long sequence, InvoiceStatus status) {
        Invoice invoice = Invoice.builder()
                .invoiceType(InvoiceType.INVOICE)
                .invoiceSequence(sequence)
                .invoiceNumber(String.format("%010d", sequence))
                .currency(InvoiceCurrency.BGN)
                .status(status)
                .issueDate(LocalDate.of(2026, 8, 1))
                .dueDate(LocalDate.of(2026, 8, 15))
                .client(client)
                .clientDisplayName(client.getDisplayName())
                .clientCompanyName(client.getCompanyName())
                .clientLegalRepresentative(client.getLegalRepresentative())
                .clientEmail(client.getEmail())
                .clientPhoneNumber(client.getPhoneNumber())
                .clientVatRegistered(client.isVatRegistered())
                .clientVatNumber(client.getVatNumber())
                .clientCountry(client.getCountry())
                .clientAddress(client.getAddress())
                .build();
        ReflectionTestUtils.setField(invoice, "id", id);
        invoice.addLineItem(InvoiceLineItem.builder()
                .description("Consulting")
                .quantity(new BigDecimal("2.00"))
                .measurementUnit(MeasurementUnit.HOUR)
                .unitPrice(new BigDecimal("50.00"))
                .vatRate(VatRate.TWENTY)
                .build());
        return invoice;
    }

    private static final class FakeInvoiceRepository {

        private final Map<UUID, Invoice> invoices = new LinkedHashMap<>();

        private boolean findAllCalled;
        private String lastSearchedCompanyName;
        private List<Invoice> searchResult = List.of();
        private Invoice savedInvoice;
        private int flushCount;

        private void addInvoice(Invoice invoice) {
            invoices.put(invoice.getId(), invoice);
        }

        private InvoiceRepository repository() {
            return proxy(InvoiceRepository.class, (proxy, method, args) -> switch (method.getName()) {
                case "findAll" -> {
                    findAllCalled = true;
                    yield invoices.values()
                            .stream()
                            .sorted(Comparator.comparing(Invoice::getInvoiceSequence).reversed())
                            .toList();
                }
                case "findByClientCompanyNameContainingIgnoreCaseOrderByInvoiceSequenceDesc" -> {
                    lastSearchedCompanyName = (String) args[0];
                    yield searchResult;
                }
                case "findById" -> Optional.ofNullable(invoices.get((UUID) args[0]));
                case "findTopByOrderByInvoiceSequenceDesc" -> invoices.values()
                        .stream()
                        .max(Comparator.comparing(Invoice::getInvoiceSequence));
                case "save" -> {
                    savedInvoice = (Invoice) args[0];
                    yield savedInvoice;
                }
                case "flush" -> {
                    flushCount++;
                    yield null;
                }
                case "findAllByStatusAndDueDateBefore" -> invoices.values()
                        .stream()
                        .filter(invoice -> invoice.getStatus() == args[0])
                        .filter(invoice -> invoice.getDueDate().isBefore((LocalDate) args[1]))
                        .toList();
                case "findAllByStatusAndDueDateGreaterThanEqual" -> invoices.values()
                        .stream()
                        .filter(invoice -> invoice.getStatus() == args[0])
                        .filter(invoice -> !invoice.getDueDate().isBefore((LocalDate) args[1]))
                        .toList();
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

    private static final class FakeInvoiceHistoryIntegrationService implements InvoiceHistoryIntegrationService {

        private final List<InvoiceHistoryCreateRequestDTO> createdRequests = new ArrayList<>();

        @Override
        public InvoiceHistoryResponseDTO createHistoryRecord(InvoiceHistoryCreateRequestDTO invoiceHistoryCreateRequestDTO) {
            createdRequests.add(invoiceHistoryCreateRequestDTO);
            return null;
        }

        @Override
        public List<InvoiceHistoryResponseDTO> findHistoryByInvoiceId(UUID invoiceId) {
            return List.of();
        }

        @Override
        public void clearHistoryByInvoiceId(UUID invoiceId) {
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
