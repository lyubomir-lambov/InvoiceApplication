package bg.softuni.invoiceapplication.service.impl;

import bg.softuni.invoiceapplication.client.InvoiceHistoryClient;
import bg.softuni.invoiceapplication.exception.ApplicationException;
import bg.softuni.invoiceapplication.model.dto.invoicehistory.InvoiceHistoryCreateRequestDTO;
import bg.softuni.invoiceapplication.model.dto.invoicehistory.InvoiceHistoryResponseDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InvoiceHistoryIntegrationServiceImplTest {

    private static final UUID INVOICE_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final String API_KEY = "lambi-invoice-history-api-key";

    private ObjectMapper objectMapper;
    private FakeInvoiceHistoryClient fakeInvoiceHistoryClient;
    private InvoiceHistoryIntegrationServiceImpl invoiceHistoryIntegrationService;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        fakeInvoiceHistoryClient = new FakeInvoiceHistoryClient();
        invoiceHistoryIntegrationService = new InvoiceHistoryIntegrationServiceImpl(
                fakeInvoiceHistoryClient,
                objectMapper,
                API_KEY);
    }

    @Test
    void createHistoryRecord_shouldCallClientWithApiKeyAndAttachSnapshot_whenResponseHasSnapshotJson() {
        InvoiceHistoryCreateRequestDTO requestDTO = createRequestDTO();

        InvoiceHistoryResponseDTO result = invoiceHistoryIntegrationService.createHistoryRecord(requestDTO);

        assertThat(fakeInvoiceHistoryClient.createApiKey()).isEqualTo(API_KEY);
        assertThat(fakeInvoiceHistoryClient.createdRequest()).isSameAs(requestDTO);
        assertThat(result.getSnapshot()).isNotNull();
        assertThat(result.getSnapshot().getInvoiceNumber()).isEqualTo("0000000001");
        assertThat(result.getSnapshot().getLineItems()).hasSize(1);
        assertThat(result.getSnapshot().getLineItems().get(0).getVatRate()).isEqualTo("20%");
    }

    @Test
    void findHistoryByInvoiceId_shouldCallClientWithApiKeyAndAttachSnapshots() {
        List<InvoiceHistoryResponseDTO> result = invoiceHistoryIntegrationService.findHistoryByInvoiceId(INVOICE_ID);

        assertThat(fakeInvoiceHistoryClient.findApiKey()).isEqualTo(API_KEY);
        assertThat(fakeInvoiceHistoryClient.requestedInvoiceId()).isEqualTo(INVOICE_ID);
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getSnapshot()).isNotNull();
        assertThat(result.get(0).getSnapshot().getInvoiceNumber()).isEqualTo("0000000001");
        assertThat(result.get(1).getSnapshot()).isNull();
    }

    @Test
    void clearHistoryByInvoiceId_shouldCallClientWithApiKeyAndInvoiceId() {
        invoiceHistoryIntegrationService.clearHistoryByInvoiceId(INVOICE_ID);

        assertThat(fakeInvoiceHistoryClient.clearApiKey()).isEqualTo(API_KEY);
        assertThat(fakeInvoiceHistoryClient.deletedInvoiceId()).isEqualTo(INVOICE_ID);
    }

    @Test
    void createHistoryRecord_shouldThrowApplicationException_whenSnapshotJsonIsInvalid() {
        fakeInvoiceHistoryClient.returnInvalidSnapshotJson();

        assertThatThrownBy(() -> invoiceHistoryIntegrationService.createHistoryRecord(createRequestDTO()))
                .isInstanceOf(ApplicationException.class)
                .hasMessage("Could not read invoice history snapshot");
    }

    private InvoiceHistoryCreateRequestDTO createRequestDTO() {
        return InvoiceHistoryCreateRequestDTO.builder()
                .invoiceId(INVOICE_ID)
                .invoiceNumber("0000000001")
                .invoiceType("INVOICE")
                .invoiceStatus("ISSUED")
                .action("CREATED")
                .currency("BGN")
                .clientDisplayName("Test Client")
                .clientCompanyName("Test Company Ltd.")
                .issueDate(LocalDate.of(2026, 8, 2))
                .dueDate(LocalDate.of(2026, 8, 15))
                .totalWithoutVat(new BigDecimal("100.00"))
                .totalVat(new BigDecimal("20.00"))
                .totalWithVat(new BigDecimal("120.00"))
                .snapshotJson(createSnapshotJson())
                .performedByUsername("admin")
                .build();
    }

    private static String createSnapshotJson() {
        return """
                {
                  "id": "11111111-1111-1111-1111-111111111111",
                  "invoiceType": "INVOICE",
                  "invoiceNumber": "0000000001",
                  "currency": "BGN",
                  "status": "ISSUED",
                  "issueDate": "2026-08-02",
                  "dueDate": "2026-08-15",
                  "clientDisplayName": "Test Client",
                  "clientCompanyName": "Test Company Ltd.",
                  "clientLegalRepresentative": "Ivan Ivanov",
                  "clientEmail": "client@example.com",
                  "clientPhoneNumber": "+359888123456",
                  "clientVatRegistered": true,
                  "clientVatNumber": "BG123456789",
                  "clientCountry": "BULGARIA",
                  "clientAddress": "Sofia, Bulgaria",
                  "lineItems": [
                    {
                      "description": "Development",
                      "quantity": 7.00,
                      "measurementUnit": "SERVICE",
                      "unitPrice": 2.00,
                      "vatRate": "20%",
                      "lineTotalWithoutVat": 14.00,
                      "vatAmount": 2.80,
                      "lineTotalWithVat": 16.80
                    }
                  ],
                  "totalWithoutVat": 14.00,
                  "totalVat": 2.80,
                  "totalWithVat": 16.80
                }
                """;
    }

    private static class FakeInvoiceHistoryClient implements InvoiceHistoryClient {

        private String createApiKey;
        private String findApiKey;
        private String clearApiKey;
        private InvoiceHistoryCreateRequestDTO createdRequest;
        private UUID requestedInvoiceId;
        private UUID deletedInvoiceId;
        private boolean returnInvalidSnapshotJson;

        @Override
        public InvoiceHistoryResponseDTO createHistoryRecord(String apiKey,
                                                             InvoiceHistoryCreateRequestDTO invoiceHistoryCreateRequestDTO) {
            this.createApiKey = apiKey;
            this.createdRequest = invoiceHistoryCreateRequestDTO;
            return createResponseDTO(1, returnInvalidSnapshotJson ? "{invalid-json" : createSnapshotJson());
        }

        @Override
        public List<InvoiceHistoryResponseDTO> findHistoryByInvoiceId(String apiKey, UUID invoiceId) {
            this.findApiKey = apiKey;
            this.requestedInvoiceId = invoiceId;
            return List.of(
                    createResponseDTO(2, createSnapshotJson()),
                    createResponseDTO(1, null)
            );
        }

        @Override
        public void clearHistoryByInvoiceId(String apiKey, UUID invoiceId) {
            this.clearApiKey = apiKey;
            this.deletedInvoiceId = invoiceId;
        }

        private String createApiKey() {
            return createApiKey;
        }

        private String findApiKey() {
            return findApiKey;
        }

        private String clearApiKey() {
            return clearApiKey;
        }

        private InvoiceHistoryCreateRequestDTO createdRequest() {
            return createdRequest;
        }

        private UUID requestedInvoiceId() {
            return requestedInvoiceId;
        }

        private UUID deletedInvoiceId() {
            return deletedInvoiceId;
        }

        private void returnInvalidSnapshotJson() {
            this.returnInvalidSnapshotJson = true;
        }

        private static InvoiceHistoryResponseDTO createResponseDTO(Integer revisionNumber, String snapshotJson) {
            return InvoiceHistoryResponseDTO.builder()
                    .invoiceId(INVOICE_ID)
                    .invoiceNumber("0000000001")
                    .invoiceType("INVOICE")
                    .invoiceStatus("ISSUED")
                    .revisionNumber(revisionNumber)
                    .action("CREATED")
                    .currency("BGN")
                    .clientDisplayName("Test Client")
                    .clientCompanyName("Test Company Ltd.")
                    .issueDate(LocalDate.of(2026, 8, 2))
                    .dueDate(LocalDate.of(2026, 8, 15))
                    .totalWithoutVat(new BigDecimal("100.00"))
                    .totalVat(new BigDecimal("20.00"))
                    .totalWithVat(new BigDecimal("120.00"))
                    .snapshotJson(snapshotJson)
                    .performedByUsername("admin")
                    .build();
        }
    }
}
