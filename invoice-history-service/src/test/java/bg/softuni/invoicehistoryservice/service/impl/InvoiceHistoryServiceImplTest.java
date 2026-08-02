package bg.softuni.invoicehistoryservice.service.impl;

import bg.softuni.invoicehistoryservice.exception.InvalidInvoiceHistoryRequestException;
import bg.softuni.invoicehistoryservice.mapper.InvoiceHistoryMapper;
import bg.softuni.invoicehistoryservice.model.dto.InvoiceHistoryCreateRequestDTO;
import bg.softuni.invoicehistoryservice.model.dto.InvoiceHistoryResponseDTO;
import bg.softuni.invoicehistoryservice.model.entity.InvoiceHistoryRecord;
import bg.softuni.invoicehistoryservice.model.enums.InvoiceHistoryAction;
import bg.softuni.invoicehistoryservice.repository.InvoiceHistoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InvoiceHistoryServiceImplTest {

    private static final UUID INVOICE_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");

    private InvoiceHistoryMapper invoiceHistoryMapper;

    @BeforeEach
    void setUp() {
        invoiceHistoryMapper = new InvoiceHistoryMapper();
    }

    @Test
    void createHistoryRecord_shouldCreateFirstRevision_whenInvoiceHasNoHistory() {
        InvoiceHistoryCreateRequestDTO requestDTO = createRequestDTO();
        FakeInvoiceHistoryRepository fakeRepository = new FakeInvoiceHistoryRepository(Optional.empty());
        InvoiceHistoryServiceImpl invoiceHistoryService = new InvoiceHistoryServiceImpl(fakeRepository.repository(), invoiceHistoryMapper);

        invoiceHistoryService.createHistoryRecord(requestDTO);

        assertThat(fakeRepository.savedRecord().getRevisionNumber()).isEqualTo(1);
    }

    @Test
    void createHistoryRecord_shouldCreateNextRevision_whenInvoiceHasHistory() {
        InvoiceHistoryCreateRequestDTO requestDTO = createRequestDTO();
        InvoiceHistoryRecord lastRecord = InvoiceHistoryRecord.builder()
                .invoiceId(INVOICE_ID)
                .revisionNumber(3)
                .build();
        FakeInvoiceHistoryRepository fakeRepository = new FakeInvoiceHistoryRepository(Optional.of(lastRecord));
        InvoiceHistoryServiceImpl invoiceHistoryService = new InvoiceHistoryServiceImpl(fakeRepository.repository(), invoiceHistoryMapper);

        invoiceHistoryService.createHistoryRecord(requestDTO);

        assertThat(fakeRepository.savedRecord().getRevisionNumber()).isEqualTo(4);
    }

    @Test
    void createHistoryRecord_shouldReturnResponseDTOWithExpectedData() {
        InvoiceHistoryCreateRequestDTO requestDTO = createRequestDTO();
        FakeInvoiceHistoryRepository fakeRepository = new FakeInvoiceHistoryRepository(Optional.empty());
        InvoiceHistoryServiceImpl invoiceHistoryService = new InvoiceHistoryServiceImpl(fakeRepository.repository(), invoiceHistoryMapper);

        InvoiceHistoryResponseDTO result = invoiceHistoryService.createHistoryRecord(requestDTO);

        assertThat(result.getInvoiceId()).isEqualTo(INVOICE_ID);
        assertThat(result.getInvoiceNumber()).isEqualTo("0000000001");
        assertThat(result.getAction()).isEqualTo(InvoiceHistoryAction.CREATED);
        assertThat(result.getRevisionNumber()).isEqualTo(1);
        assertThat(result.getSnapshotJson()).isEqualTo("{\"invoiceNumber\":\"0000000001\"}");
        assertThat(result.getPerformedByUsername()).isEqualTo("admin");
    }

    @Test
    void createHistoryRecord_shouldThrowException_whenRequestIsNull() {
        FakeInvoiceHistoryRepository fakeRepository = new FakeInvoiceHistoryRepository(Optional.empty());
        InvoiceHistoryServiceImpl invoiceHistoryService = new InvoiceHistoryServiceImpl(fakeRepository.repository(), invoiceHistoryMapper);

        assertThatThrownBy(() -> invoiceHistoryService.createHistoryRecord(null))
                .isInstanceOf(InvalidInvoiceHistoryRequestException.class)
                .hasMessage("Invoice history create request must not be null");
    }

    @Test
    void findHistoryByInvoiceId_shouldReturnResponseDTOs_whenHistoryExists() {
        FakeInvoiceHistoryRepository fakeRepository = new FakeInvoiceHistoryRepository(
                Optional.empty(),
                List.of(
                        createRecord(2, InvoiceHistoryAction.UPDATED),
                        createRecord(1, InvoiceHistoryAction.CREATED)
                ));
        InvoiceHistoryServiceImpl invoiceHistoryService = new InvoiceHistoryServiceImpl(fakeRepository.repository(), invoiceHistoryMapper);

        List<InvoiceHistoryResponseDTO> result = invoiceHistoryService.findHistoryByInvoiceId(INVOICE_ID);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getInvoiceId()).isEqualTo(INVOICE_ID);
        assertThat(result.get(0).getRevisionNumber()).isEqualTo(2);
        assertThat(result.get(0).getAction()).isEqualTo(InvoiceHistoryAction.UPDATED);
        assertThat(result.get(1).getRevisionNumber()).isEqualTo(1);
        assertThat(result.get(1).getAction()).isEqualTo(InvoiceHistoryAction.CREATED);
    }

    @Test
    void findHistoryByInvoiceId_shouldThrowException_whenInvoiceIdIsNull() {
        FakeInvoiceHistoryRepository fakeRepository = new FakeInvoiceHistoryRepository(Optional.empty());
        InvoiceHistoryServiceImpl invoiceHistoryService = new InvoiceHistoryServiceImpl(fakeRepository.repository(), invoiceHistoryMapper);

        assertThatThrownBy(() -> invoiceHistoryService.findHistoryByInvoiceId(null))
                .isInstanceOf(InvalidInvoiceHistoryRequestException.class)
                .hasMessage("Invoice id must not be null");
    }

    @Test
    void clearHistoryByInvoiceId_shouldDeleteHistory_whenInvoiceIdIsValid() {
        FakeInvoiceHistoryRepository fakeRepository = new FakeInvoiceHistoryRepository(Optional.empty());
        InvoiceHistoryServiceImpl invoiceHistoryService = new InvoiceHistoryServiceImpl(fakeRepository.repository(), invoiceHistoryMapper);

        invoiceHistoryService.clearHistoryByInvoiceId(INVOICE_ID);

        assertThat(fakeRepository.deletedInvoiceId()).isEqualTo(INVOICE_ID);
    }

    @Test
    void clearHistoryByInvoiceId_shouldThrowException_whenInvoiceIdIsNull() {
        FakeInvoiceHistoryRepository fakeRepository = new FakeInvoiceHistoryRepository(Optional.empty());
        InvoiceHistoryServiceImpl invoiceHistoryService = new InvoiceHistoryServiceImpl(fakeRepository.repository(), invoiceHistoryMapper);

        assertThatThrownBy(() -> invoiceHistoryService.clearHistoryByInvoiceId(null))
                .isInstanceOf(InvalidInvoiceHistoryRequestException.class)
                .hasMessage("Invoice id must not be null");
    }

    private InvoiceHistoryCreateRequestDTO createRequestDTO() {
        return InvoiceHistoryCreateRequestDTO.builder()
                .invoiceId(INVOICE_ID)
                .invoiceNumber("0000000001")
                .invoiceType("INVOICE")
                .invoiceStatus("ISSUED")
                .action(InvoiceHistoryAction.CREATED)
                .currency("BGN")
                .clientDisplayName("Test Client")
                .clientCompanyName("Test Company Ltd.")
                .issueDate(LocalDate.of(2026, 8, 2))
                .dueDate(LocalDate.of(2026, 8, 15))
                .totalWithoutVat(new BigDecimal("100.00"))
                .totalVat(new BigDecimal("20.00"))
                .totalWithVat(new BigDecimal("120.00"))
                .snapshotJson("{\"invoiceNumber\":\"0000000001\"}")
                .performedByUsername("admin")
                .build();
    }

    private InvoiceHistoryRecord createRecord(Integer revisionNumber, InvoiceHistoryAction action) {
        return InvoiceHistoryRecord.builder()
                .invoiceId(INVOICE_ID)
                .invoiceNumber("0000000001")
                .invoiceType("INVOICE")
                .invoiceStatus("ISSUED")
                .revisionNumber(revisionNumber)
                .action(action)
                .currency("BGN")
                .clientDisplayName("Test Client")
                .clientCompanyName("Test Company Ltd.")
                .issueDate(LocalDate.of(2026, 8, 2))
                .dueDate(LocalDate.of(2026, 8, 15))
                .totalWithoutVat(new BigDecimal("100.00"))
                .totalVat(new BigDecimal("20.00"))
                .totalWithVat(new BigDecimal("120.00"))
                .snapshotJson("{\"invoiceNumber\":\"0000000001\"}")
                .performedByUsername("admin")
                .build();
    }

    private static class FakeInvoiceHistoryRepository {

        private final Optional<InvoiceHistoryRecord> topRecord;
        private final List<InvoiceHistoryRecord> records;
        private InvoiceHistoryRecord savedRecord;
        private UUID deletedInvoiceId;

        private FakeInvoiceHistoryRepository(Optional<InvoiceHistoryRecord> topRecord) {
            this(topRecord, List.of());
        }

        private FakeInvoiceHistoryRepository(Optional<InvoiceHistoryRecord> topRecord, List<InvoiceHistoryRecord> records) {
            this.topRecord = topRecord;
            this.records = records;
        }

        private InvoiceHistoryRepository repository() {
            return (InvoiceHistoryRepository) Proxy.newProxyInstance(
                    InvoiceHistoryRepository.class.getClassLoader(),
                    new Class[]{InvoiceHistoryRepository.class},
                    (proxy, method, args) -> switch (method.getName()) {
                        case "findTopByInvoiceIdOrderByRevisionNumberDesc" -> topRecord;
                        case "findByInvoiceIdOrderByRevisionNumberDesc" -> records;
                        case "deleteByInvoiceId" -> {
                            deletedInvoiceId = (UUID) args[0];
                            yield null;
                        }
                        case "save" -> {
                            savedRecord = (InvoiceHistoryRecord) args[0];
                            yield savedRecord;
                        }
                        case "toString" -> "FakeInvoiceHistoryRepository";
                        case "hashCode" -> System.identityHashCode(proxy);
                        case "equals" -> proxy == args[0];
                        default -> throw new UnsupportedOperationException("Method not supported by test fake: " + method.getName());
                    });
        }

        private InvoiceHistoryRecord savedRecord() {
            return savedRecord;
        }

        private UUID deletedInvoiceId() {
            return deletedInvoiceId;
        }
    }
}
