package bg.softuni.invoicehistoryservice.service.impl;

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
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

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

    private static class FakeInvoiceHistoryRepository {

        private final Optional<InvoiceHistoryRecord> topRecord;
        private InvoiceHistoryRecord savedRecord;

        private FakeInvoiceHistoryRepository(Optional<InvoiceHistoryRecord> topRecord) {
            this.topRecord = topRecord;
        }

        private InvoiceHistoryRepository repository() {
            return (InvoiceHistoryRepository) Proxy.newProxyInstance(
                    InvoiceHistoryRepository.class.getClassLoader(),
                    new Class[]{InvoiceHistoryRepository.class},
                    (proxy, method, args) -> switch (method.getName()) {
                        case "findTopByInvoiceIdOrderByRevisionNumberDesc" -> topRecord;
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
    }
}
