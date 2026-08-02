package bg.softuni.invoicehistoryservice.mapper;

import bg.softuni.invoicehistoryservice.model.dto.InvoiceHistoryCreateRequestDTO;
import bg.softuni.invoicehistoryservice.model.dto.InvoiceHistoryResponseDTO;
import bg.softuni.invoicehistoryservice.model.entity.InvoiceHistoryRecord;
import bg.softuni.invoicehistoryservice.model.enums.InvoiceHistoryAction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class InvoiceHistoryMapperTest {

    private static final UUID INVOICE_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");

    private InvoiceHistoryMapper invoiceHistoryMapper;

    @BeforeEach
    void setUp() {
        invoiceHistoryMapper = new InvoiceHistoryMapper();
    }

    @Test
    void fromCreateRequestDTOToInvoiceHistoryRecord_shouldMapFields_whenRequestIsValid() {
        InvoiceHistoryCreateRequestDTO requestDTO = createRequestDTO();

        InvoiceHistoryRecord result = invoiceHistoryMapper.fromCreateRequestDTOToInvoiceHistoryRecord(requestDTO);

        assertThat(result.getInvoiceId()).isEqualTo(INVOICE_ID);
        assertThat(result.getInvoiceNumber()).isEqualTo("0000000001");
        assertThat(result.getInvoiceType()).isEqualTo("INVOICE");
        assertThat(result.getInvoiceStatus()).isEqualTo("ISSUED");
        assertThat(result.getAction()).isEqualTo(InvoiceHistoryAction.CREATED);
        assertThat(result.getCurrency()).isEqualTo("BGN");
        assertThat(result.getClientDisplayName()).isEqualTo("Test Client");
        assertThat(result.getClientCompanyName()).isEqualTo("Test Company Ltd.");
        assertThat(result.getTotalWithoutVat()).isEqualByComparingTo("100.00");
        assertThat(result.getTotalVat()).isEqualByComparingTo("20.00");
        assertThat(result.getTotalWithVat()).isEqualByComparingTo("120.00");
        assertThat(result.getSnapshotJson()).isEqualTo("{\"invoiceNumber\":\"0000000001\"}");
        assertThat(result.getPerformedByUsername()).isEqualTo("admin");
    }

    @Test
    void fromCreateRequestDTOToInvoiceHistoryRecord_shouldReturnNull_whenRequestIsNull() {
        InvoiceHistoryRecord result = invoiceHistoryMapper.fromCreateRequestDTOToInvoiceHistoryRecord(null);

        assertThat(result).isNull();
    }

    @Test
    void fromInvoiceHistoryRecordToResponseDTO_shouldMapFields_whenRecordIsValid() {
        LocalDateTime createdOn = LocalDateTime.of(2026, 8, 2, 10, 30);
        InvoiceHistoryRecord record = createRecord();
        record.setCreatedOn(createdOn);

        InvoiceHistoryResponseDTO result = invoiceHistoryMapper.fromInvoiceHistoryRecordToResponseDTO(record);

        assertThat(result.getInvoiceId()).isEqualTo(INVOICE_ID);
        assertThat(result.getInvoiceNumber()).isEqualTo("0000000001");
        assertThat(result.getInvoiceType()).isEqualTo("INVOICE");
        assertThat(result.getInvoiceStatus()).isEqualTo("ISSUED");
        assertThat(result.getRevisionNumber()).isEqualTo(2);
        assertThat(result.getAction()).isEqualTo(InvoiceHistoryAction.UPDATED);
        assertThat(result.getCurrency()).isEqualTo("BGN");
        assertThat(result.getSnapshotJson()).isEqualTo("{\"invoiceNumber\":\"0000000001\"}");
        assertThat(result.getPerformedByUsername()).isEqualTo("admin");
        assertThat(result.getCreatedOn()).isEqualTo(createdOn);
    }

    @Test
    void fromInvoiceHistoryRecordToResponseDTO_shouldReturnNull_whenRecordIsNull() {
        InvoiceHistoryResponseDTO result = invoiceHistoryMapper.fromInvoiceHistoryRecordToResponseDTO(null);

        assertThat(result).isNull();
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

    private InvoiceHistoryRecord createRecord() {
        return InvoiceHistoryRecord.builder()
                .invoiceId(INVOICE_ID)
                .invoiceNumber("0000000001")
                .invoiceType("INVOICE")
                .invoiceStatus("ISSUED")
                .revisionNumber(2)
                .action(InvoiceHistoryAction.UPDATED)
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
}
