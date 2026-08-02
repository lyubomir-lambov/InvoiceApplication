package bg.softuni.invoicehistoryservice.web;

import bg.softuni.invoicehistoryservice.exception.GlobalExceptionHandler;
import bg.softuni.invoicehistoryservice.model.dto.InvoiceHistoryCreateRequestDTO;
import bg.softuni.invoicehistoryservice.model.dto.InvoiceHistoryResponseDTO;
import bg.softuni.invoicehistoryservice.model.enums.InvoiceHistoryAction;
import bg.softuni.invoicehistoryservice.service.InvoiceHistoryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class InvoiceHistoryControllerTest {

    private static final UUID INVOICE_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");

    private ObjectMapper objectMapper;
    private FakeInvoiceHistoryService fakeInvoiceHistoryService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        fakeInvoiceHistoryService = new FakeInvoiceHistoryService();

        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders
                .standaloneSetup(new InvoiceHistoryController(fakeInvoiceHistoryService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();
    }

    @Test
    void createHistoryRecord_shouldReturnCreatedAndResponseBody_whenRequestIsValid() throws Exception {
        InvoiceHistoryCreateRequestDTO requestDTO = createRequestDTO();

        mockMvc.perform(post("/api/invoice-history")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.invoiceId").value(INVOICE_ID.toString()))
                .andExpect(jsonPath("$.invoiceNumber").value("0000000001"))
                .andExpect(jsonPath("$.revisionNumber").value(1))
                .andExpect(jsonPath("$.action").value("CREATED"))
                .andExpect(jsonPath("$.performedByUsername").value("admin"));

        assertThat(fakeInvoiceHistoryService.createdRequest().getInvoiceId()).isEqualTo(INVOICE_ID);
    }

    @Test
    void createHistoryRecord_shouldReturnBadRequest_whenRequestIsInvalid() throws Exception {
        InvoiceHistoryCreateRequestDTO requestDTO = createRequestDTO();
        requestDTO.setInvoiceNumber("");

        mockMvc.perform(post("/api/invoice-history")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errorCode").value("validation_error"))
                .andExpect(jsonPath("$.errorTitle").value("Validation Error"));
    }

    @Test
    void findHistoryByInvoiceId_shouldReturnHistoryRecords_whenInvoiceIdIsValid() throws Exception {
        mockMvc.perform(get("/api/invoice-history/invoices/{invoiceId}", INVOICE_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].invoiceId").value(INVOICE_ID.toString()))
                .andExpect(jsonPath("$[0].revisionNumber").value(2))
                .andExpect(jsonPath("$[0].action").value("UPDATED"))
                .andExpect(jsonPath("$[1].revisionNumber").value(1))
                .andExpect(jsonPath("$[1].action").value("CREATED"));

        assertThat(fakeInvoiceHistoryService.requestedInvoiceId()).isEqualTo(INVOICE_ID);
    }

    @Test
    void clearHistoryByInvoiceId_shouldReturnNoContent_whenInvoiceIdIsValid() throws Exception {
        mockMvc.perform(delete("/api/invoice-history/invoices/{invoiceId}", INVOICE_ID))
                .andExpect(status().isNoContent());

        assertThat(fakeInvoiceHistoryService.deletedInvoiceId()).isEqualTo(INVOICE_ID);
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

    private static class FakeInvoiceHistoryService implements InvoiceHistoryService {

        private InvoiceHistoryCreateRequestDTO createdRequest;
        private UUID requestedInvoiceId;
        private UUID deletedInvoiceId;

        @Override
        public InvoiceHistoryResponseDTO createHistoryRecord(InvoiceHistoryCreateRequestDTO invoiceHistoryCreateRequestDTO) {
            this.createdRequest = invoiceHistoryCreateRequestDTO;
            return createResponseDTO(1, InvoiceHistoryAction.CREATED);
        }

        @Override
        public List<InvoiceHistoryResponseDTO> findHistoryByInvoiceId(UUID invoiceId) {
            this.requestedInvoiceId = invoiceId;
            return List.of(
                    createResponseDTO(2, InvoiceHistoryAction.UPDATED),
                    createResponseDTO(1, InvoiceHistoryAction.CREATED)
            );
        }

        @Override
        public void clearHistoryByInvoiceId(UUID invoiceId) {
            this.deletedInvoiceId = invoiceId;
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

        private static InvoiceHistoryResponseDTO createResponseDTO(Integer revisionNumber, InvoiceHistoryAction action) {
            return InvoiceHistoryResponseDTO.builder()
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
    }
}
