package bg.softuni.invoiceapplication.mapper.invoice;

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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class InvoiceMapperTest {

    private static final UUID INVOICE_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID CLIENT_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");

    private InvoiceMapper invoiceMapper;

    @BeforeEach
    void setUp() {
        invoiceMapper = new InvoiceMapper();
    }

    @Test
    void fromInvoiceToInvoiceShowAllDTO_shouldMapFieldsAndTotalAmount_whenInvoiceIsValid() {
        Invoice invoice = createInvoice();

        InvoiceShowAllDTO result = invoiceMapper.fromInvoiceToInvoiceShowAllDTO(invoice);

        assertThat(result.getId()).isEqualTo(INVOICE_ID);
        assertThat(result.getInvoiceType()).isEqualTo(InvoiceType.INVOICE);
        assertThat(result.getInvoiceNumber()).isEqualTo("0000000001");
        assertThat(result.getCurrency()).isEqualTo(InvoiceCurrency.BGN);
        assertThat(result.getStatus()).isEqualTo(InvoiceStatus.ISSUED);
        assertThat(result.getClientCompanyName()).isEqualTo("Lambi Ltd.");
        assertThat(result.getIssueDate()).isEqualTo(LocalDate.of(2026, 8, 1));
        assertThat(result.getTotalAmount()).isEqualByComparingTo("229.00");
    }

    @Test
    void fromInvoiceToInvoiceShowAllDTO_shouldUseDefaultCurrencyAndStatus_whenInvoiceValuesAreNull() {
        Invoice invoice = createInvoice();
        invoice.setCurrency(null);
        invoice.setStatus(null);

        InvoiceShowAllDTO result = invoiceMapper.fromInvoiceToInvoiceShowAllDTO(invoice);

        assertThat(result.getCurrency()).isEqualTo(InvoiceCurrency.BGN);
        assertThat(result.getStatus()).isEqualTo(InvoiceStatus.ISSUED);
    }

    @Test
    void fromInvoiceToInvoiceShowAllDTO_shouldReturnNull_whenInvoiceIsNull() {
        InvoiceShowAllDTO result = invoiceMapper.fromInvoiceToInvoiceShowAllDTO(null);

        assertThat(result).isNull();
    }

    @Test
    void fromAllInvoicesToInvoiceShowAllDTOs_shouldMapInvoices_whenInvoicesExist() {
        Invoice invoice = createInvoice();

        List<InvoiceShowAllDTO> result = invoiceMapper.fromAllInvoicesToInvoiceShowAllDTOs(List.of(invoice));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getInvoiceNumber()).isEqualTo("0000000001");
        assertThat(result.get(0).getTotalAmount()).isEqualByComparingTo("229.00");
    }

    @Test
    void fromAllInvoicesToInvoiceShowAllDTOs_shouldReturnNull_whenInvoicesAreNull() {
        List<InvoiceShowAllDTO> result = invoiceMapper.fromAllInvoicesToInvoiceShowAllDTOs(null);

        assertThat(result).isNull();
    }

    @Test
    void fromInvoiceToInvoiceDetailsDTO_shouldMapFieldsLineItemsAndTotals_whenInvoiceIsValid() {
        Invoice invoice = createInvoice();

        InvoiceDetailsDTO result = invoiceMapper.fromInvoiceToInvoiceDetailsDTO(invoice);

        assertThat(result.getId()).isEqualTo(INVOICE_ID);
        assertThat(result.getInvoiceNumber()).isEqualTo("0000000001");
        assertThat(result.getClientDisplayName()).isEqualTo("Lambi");
        assertThat(result.getClientLegalRepresentative()).isEqualTo("Lyubomir Lambov");
        assertThat(result.getClientEmail()).isEqualTo("client@example.com");
        assertThat(result.getClientPhoneNumber()).isEqualTo("+359888123456");
        assertThat(result.isClientVatRegistered()).isTrue();
        assertThat(result.getClientVatNumber()).isEqualTo("BG123456789");
        assertThat(result.getClientCountry()).isEqualTo(Country.BULGARIA);
        assertThat(result.getClientAddress()).isEqualTo("Sofia");
        assertThat(result.getDueDate()).isEqualTo(LocalDate.of(2026, 8, 15));
        assertThat(result.getSubtotalAmount()).isEqualByComparingTo("200.00");
        assertThat(result.getVatAmount()).isEqualByComparingTo("29.00");
        assertThat(result.getTotalAmount()).isEqualByComparingTo("229.00");
        assertThat(result.getLineItems()).hasSize(2);
        assertThat(result.getLineItems().get(0).getDescription()).isEqualTo("Consulting");
        assertThat(result.getLineItems().get(0).getLineTotalWithVat()).isEqualByComparingTo("120.00");
    }

    @Test
    void fromInvoiceToInvoiceDetailsDTO_shouldReturnNull_whenInvoiceIsNull() {
        InvoiceDetailsDTO result = invoiceMapper.fromInvoiceToInvoiceDetailsDTO(null);

        assertThat(result).isNull();
    }

    @Test
    void fromInvoiceToInvoiceEditRequestDTO_shouldMapEditableFields_whenInvoiceIsValid() {
        Invoice invoice = createInvoice();

        InvoiceEditRequestDTO result = invoiceMapper.fromInvoiceToInvoiceEditRequestDTO(invoice);

        assertThat(result.getId()).isEqualTo(INVOICE_ID);
        assertThat(result.getInvoiceType()).isEqualTo(InvoiceType.INVOICE);
        assertThat(result.getInvoiceNumber()).isEqualTo("0000000001");
        assertThat(result.getCurrency()).isEqualTo(InvoiceCurrency.BGN);
        assertThat(result.getStatus()).isEqualTo(InvoiceStatus.ISSUED);
        assertThat(result.getIssueDate()).isEqualTo(LocalDate.of(2026, 8, 1));
        assertThat(result.getDueDate()).isEqualTo(LocalDate.of(2026, 8, 15));
        assertThat(result.getClientId()).isEqualTo(CLIENT_ID);
        assertThat(result.getLineItems()).hasSize(2);
        assertThat(result.getLineItems().get(1).getDescription()).isEqualTo("Training");
        assertThat(result.getLineItems().get(1).getVatRate()).isEqualTo(VatRate.NINE);
    }

    @Test
    void fromInvoiceToInvoiceEditRequestDTO_shouldReturnNull_whenInvoiceIsNull() {
        InvoiceEditRequestDTO result = invoiceMapper.fromInvoiceToInvoiceEditRequestDTO(null);

        assertThat(result).isNull();
    }

    @Test
    void fromInvoiceLineItemCreateRequestDTOToInvoiceLineItem_shouldMapFields_whenRequestIsValid() {
        InvoiceLineItemCreateRequestDTO requestDTO = InvoiceLineItemCreateRequestDTO.builder()
                .description("Development")
                .quantity(new BigDecimal("3.00"))
                .measurementUnit(MeasurementUnit.HOUR)
                .unitPrice(new BigDecimal("50.00"))
                .vatRate(VatRate.TWENTY)
                .build();

        InvoiceLineItem result = invoiceMapper.fromInvoiceLineItemCreateRequestDTOToInvoiceLineItem(requestDTO);

        assertThat(result.getDescription()).isEqualTo("Development");
        assertThat(result.getQuantity()).isEqualByComparingTo("3.00");
        assertThat(result.getMeasurementUnit()).isEqualTo(MeasurementUnit.HOUR);
        assertThat(result.getUnitPrice()).isEqualByComparingTo("50.00");
        assertThat(result.getVatRate()).isEqualTo(VatRate.TWENTY);
    }

    @Test
    void fromInvoiceLineItemCreateRequestDTOToInvoiceLineItem_shouldReturnNull_whenRequestIsNull() {
        InvoiceLineItem result = invoiceMapper.fromInvoiceLineItemCreateRequestDTOToInvoiceLineItem(null);

        assertThat(result).isNull();
    }

    private Invoice createInvoice() {
        Client client = Client.builder()
                .displayName("Lambi")
                .companyName("Lambi Ltd.")
                .build();
        ReflectionTestUtils.setField(client, "id", CLIENT_ID);

        Invoice invoice = Invoice.builder()
                .invoiceType(InvoiceType.INVOICE)
                .invoiceSequence(1L)
                .invoiceNumber("0000000001")
                .currency(InvoiceCurrency.BGN)
                .status(InvoiceStatus.ISSUED)
                .issueDate(LocalDate.of(2026, 8, 1))
                .dueDate(LocalDate.of(2026, 8, 15))
                .client(client)
                .clientDisplayName("Lambi")
                .clientCompanyName("Lambi Ltd.")
                .clientLegalRepresentative("Lyubomir Lambov")
                .clientEmail("client@example.com")
                .clientPhoneNumber("+359888123456")
                .clientVatRegistered(true)
                .clientVatNumber("BG123456789")
                .clientCountry(Country.BULGARIA)
                .clientAddress("Sofia")
                .build();
        ReflectionTestUtils.setField(invoice, "id", INVOICE_ID);

        invoice.addLineItem(createLineItem("Consulting", "2.00", "50.00", VatRate.TWENTY));
        invoice.addLineItem(createLineItem("Training", "1.00", "100.00", VatRate.NINE));

        return invoice;
    }

    private InvoiceLineItem createLineItem(String description,
                                           String quantity,
                                           String unitPrice,
                                           VatRate vatRate) {
        return InvoiceLineItem.builder()
                .description(description)
                .quantity(new BigDecimal(quantity))
                .measurementUnit(MeasurementUnit.HOUR)
                .unitPrice(new BigDecimal(unitPrice))
                .vatRate(vatRate)
                .build();
    }
}
