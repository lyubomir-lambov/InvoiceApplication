package bg.softuni.invoiceapplication.mapper.invoicehistory;

import bg.softuni.invoiceapplication.model.dto.invoicehistory.InvoiceHistoryCreateRequestDTO;
import bg.softuni.invoiceapplication.model.entity.Invoice;
import bg.softuni.invoiceapplication.model.entity.InvoiceLineItem;
import bg.softuni.invoiceapplication.model.enums.Country;
import bg.softuni.invoiceapplication.model.enums.InvoiceCurrency;
import bg.softuni.invoiceapplication.model.enums.InvoiceStatus;
import bg.softuni.invoiceapplication.model.enums.InvoiceType;
import bg.softuni.invoiceapplication.model.enums.MeasurementUnit;
import bg.softuni.invoiceapplication.model.enums.VatRate;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class InvoiceHistoryMapperTest {

    private static final UUID INVOICE_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");

    private ObjectMapper objectMapper;
    private InvoiceHistoryMapper invoiceHistoryMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        invoiceHistoryMapper = new InvoiceHistoryMapper(objectMapper);
    }

    @Test
    void fromInvoiceToHistoryCreateRequestDTO_shouldMapMainInvoiceFieldsAndTotals_whenInvoiceIsValid() {
        Invoice invoice = createInvoice();

        InvoiceHistoryCreateRequestDTO result = invoiceHistoryMapper.fromInvoiceToHistoryCreateRequestDTO(
                invoice,
                "CREATED",
                "admin");

        assertThat(result.getInvoiceId()).isEqualTo(INVOICE_ID);
        assertThat(result.getInvoiceNumber()).isEqualTo("0000000001");
        assertThat(result.getInvoiceType()).isEqualTo("INVOICE");
        assertThat(result.getInvoiceStatus()).isEqualTo("ISSUED");
        assertThat(result.getAction()).isEqualTo("CREATED");
        assertThat(result.getCurrency()).isEqualTo("BGN");
        assertThat(result.getClientDisplayName()).isEqualTo("Test Client");
        assertThat(result.getClientCompanyName()).isEqualTo("Test Company Ltd.");
        assertThat(result.getIssueDate()).isEqualTo(LocalDate.of(2026, 8, 2));
        assertThat(result.getDueDate()).isEqualTo(LocalDate.of(2026, 8, 15));
        assertThat(result.getTotalWithoutVat()).isEqualByComparingTo("24.00");
        assertThat(result.getTotalVat()).isEqualByComparingTo("2.80");
        assertThat(result.getTotalWithVat()).isEqualByComparingTo("26.80");
        assertThat(result.getPerformedByUsername()).isEqualTo("admin");
    }

    @Test
    void fromInvoiceToHistoryCreateRequestDTO_shouldCreateSnapshotJsonWithFormattedLineItems_whenInvoiceIsValid() throws Exception {
        Invoice invoice = createInvoice();

        InvoiceHistoryCreateRequestDTO result = invoiceHistoryMapper.fromInvoiceToHistoryCreateRequestDTO(
                invoice,
                "CREATED",
                "admin");

        JsonNode snapshot = objectMapper.readTree(result.getSnapshotJson());

        assertThat(snapshot.get("id").asText()).isEqualTo(INVOICE_ID.toString());
        assertThat(snapshot.get("invoiceNumber").asText()).isEqualTo("0000000001");
        assertThat(snapshot.get("status").asText()).isEqualTo("ISSUED");
        assertThat(snapshot.get("clientCountry").asText()).isEqualTo("BULGARIA");
        assertThat(snapshot.get("totalWithoutVat").decimalValue()).isEqualByComparingTo("24.00");
        assertThat(snapshot.get("totalVat").decimalValue()).isEqualByComparingTo("2.80");
        assertThat(snapshot.get("totalWithVat").decimalValue()).isEqualByComparingTo("26.80");

        JsonNode firstLineItem = snapshot.get("lineItems").get(0);
        assertThat(firstLineItem.get("quantity").decimalValue()).isEqualByComparingTo("7.00");
        assertThat(firstLineItem.get("unitPrice").decimalValue()).isEqualByComparingTo("2.00");
        assertThat(firstLineItem.get("vatRate").asText()).isEqualTo("20%");
        assertThat(firstLineItem.get("lineTotalWithVat").decimalValue()).isEqualByComparingTo("16.80");

        JsonNode secondLineItem = snapshot.get("lineItems").get(1);
        assertThat(secondLineItem.get("vatRate").asText()).isEqualTo("0%");
        assertThat(secondLineItem.get("lineTotalWithVat").decimalValue()).isEqualByComparingTo("10.00");
    }

    @Test
    void fromInvoiceToHistoryCreateRequestDTO_shouldReturnNull_whenInvoiceIsNull() {
        InvoiceHistoryCreateRequestDTO result = invoiceHistoryMapper.fromInvoiceToHistoryCreateRequestDTO(
                null,
                "CREATED",
                "admin");

        assertThat(result).isNull();
    }

    private Invoice createInvoice() {
        Invoice invoice = Invoice.builder()
                .invoiceType(InvoiceType.INVOICE)
                .invoiceSequence(1L)
                .invoiceNumber("0000000001")
                .currency(InvoiceCurrency.BGN)
                .status(InvoiceStatus.ISSUED)
                .issueDate(LocalDate.of(2026, 8, 2))
                .dueDate(LocalDate.of(2026, 8, 15))
                .clientDisplayName("Test Client")
                .clientCompanyName("Test Company Ltd.")
                .clientLegalRepresentative("Ivan Ivanov")
                .clientEmail("client@example.com")
                .clientPhoneNumber("+359888123456")
                .clientVatRegistered(true)
                .clientVatNumber("BG123456789")
                .clientCountry(Country.BULGARIA)
                .clientAddress("Sofia, Bulgaria")
                .build();
        ReflectionTestUtils.setField(invoice, "id", INVOICE_ID);

        invoice.addLineItem(createLineItem("Development", "7", "2", VatRate.TWENTY));
        invoice.addLineItem(createLineItem("Consulting", "1", "10", VatRate.ZERO));

        return invoice;
    }

    private InvoiceLineItem createLineItem(String description, String quantity, String unitPrice, VatRate vatRate) {
        return InvoiceLineItem.builder()
                .description(description)
                .quantity(new BigDecimal(quantity))
                .measurementUnit(MeasurementUnit.SERVICE)
                .unitPrice(new BigDecimal(unitPrice))
                .vatRate(vatRate)
                .build();
    }
}
