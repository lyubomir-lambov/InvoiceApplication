package bg.softuni.invoiceapplication.web;

import bg.softuni.invoiceapplication.model.dto.clients.ClientSelectDTO;
import bg.softuni.invoiceapplication.model.dto.clients.ClientShowAllDTO;
import bg.softuni.invoiceapplication.model.dto.invoicehistory.InvoiceHistoryCreateRequestDTO;
import bg.softuni.invoiceapplication.model.dto.invoicehistory.InvoiceHistoryResponseDTO;
import bg.softuni.invoiceapplication.model.dto.invoices.InvoiceCreateRequestDTO;
import bg.softuni.invoiceapplication.model.dto.invoices.InvoiceDetailsDTO;
import bg.softuni.invoiceapplication.model.dto.invoices.InvoiceEditRequestDTO;
import bg.softuni.invoiceapplication.model.dto.invoices.InvoiceShowAllDTO;
import bg.softuni.invoiceapplication.model.entity.Client;
import bg.softuni.invoiceapplication.model.entity.Invoice;
import bg.softuni.invoiceapplication.model.enums.InvoiceCurrency;
import bg.softuni.invoiceapplication.model.enums.InvoiceStatus;
import bg.softuni.invoiceapplication.model.enums.InvoiceType;
import bg.softuni.invoiceapplication.model.enums.MeasurementUnit;
import bg.softuni.invoiceapplication.model.enums.UserRole;
import bg.softuni.invoiceapplication.model.enums.VatRate;
import bg.softuni.invoiceapplication.security.AuthenticatedUserDetails;
import bg.softuni.invoiceapplication.service.ClientService;
import bg.softuni.invoiceapplication.service.InvoiceHistoryIntegrationService;
import bg.softuni.invoiceapplication.service.InvoiceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class InvoiceControllerTest {

    private static final UUID INVOICE_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID CLIENT_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");

    private FakeInvoiceService invoiceService;
    private FakeClientService clientService;
    private FakeInvoiceHistoryIntegrationService invoiceHistoryIntegrationService;
    private InvoiceController invoiceController;

    @BeforeEach
    void setUp() {
        invoiceService = new FakeInvoiceService();
        clientService = new FakeClientService();
        invoiceHistoryIntegrationService = new FakeInvoiceHistoryIntegrationService();
        invoiceController = new InvoiceController(invoiceService, clientService, invoiceHistoryIntegrationService);
    }

    @Test
    void invoices_shouldAddInvoicesAndCompanyNameToModel() {
        Model model = new ExtendedModelMap();

        String viewName = invoiceController.invoices("Lambi", model);

        assertThat(viewName).isEqualTo("invoices");
        assertThat(model.getAttribute("invoices")).isEqualTo(invoiceService.invoices);
        assertThat(model.getAttribute("companyName")).isEqualTo("Lambi");
        assertThat(invoiceService.lastCompanyName).isEqualTo("Lambi");
    }

    @Test
    void showCreateInvoiceForm_shouldAddFormAttributesAndPreparedInvoice() {
        Model model = new ExtendedModelMap();

        String viewName = invoiceController.showCreateInvoiceForm(model);

        assertThat(viewName).isEqualTo("invoice-create");
        assertThat(model.getAttribute("invoice")).isSameAs(invoiceService.preparedInvoice);
        assertCreateFormAttributes(model);
        assertThat(model.getAttribute("clients")).isEqualTo(clientService.activeClients);
    }

    @Test
    void showInvoice_shouldAddInvoiceAndHistoryToModel() {
        Model model = new ExtendedModelMap();

        String viewName = invoiceController.showInvoice(INVOICE_ID, model);

        assertThat(viewName).isEqualTo("invoice-details");
        assertThat(model.getAttribute("invoice")).isSameAs(invoiceService.invoiceDetails);
        assertThat(model.getAttribute("invoiceHistory")).isEqualTo(invoiceHistoryIntegrationService.history);
        assertThat(invoiceService.lastFindInvoiceId).isEqualTo(INVOICE_ID);
        assertThat(invoiceHistoryIntegrationService.lastFindHistoryInvoiceId).isEqualTo(INVOICE_ID);
    }

    @Test
    void createInvoice_shouldReturnCreateView_whenBindingResultHasErrors() {
        InvoiceCreateRequestDTO requestDTO = createInvoiceCreateRequestDTO();
        BindingResult bindingResult = new BeanPropertyBindingResult(requestDTO, "invoice");
        bindingResult.rejectValue("clientId", "client.required", "Client required");
        Model model = new ExtendedModelMap();

        String viewName = invoiceController.createInvoice(requestDTO, bindingResult, model, createUser(UserRole.USER));

        assertThat(viewName).isEqualTo("invoice-create");
        assertThat(invoiceService.createdInvoiceRequest).isNull();
        assertCreateFormAttributes(model);
    }

    @Test
    void createInvoice_shouldCreateInvoiceWithCurrentUsernameAndRedirect_whenRequestIsValid() {
        InvoiceCreateRequestDTO requestDTO = createInvoiceCreateRequestDTO();
        BindingResult bindingResult = new BeanPropertyBindingResult(requestDTO, "invoice");
        Model model = new ExtendedModelMap();

        String viewName = invoiceController.createInvoice(requestDTO, bindingResult, model, createUser(UserRole.USER));

        assertThat(viewName).isEqualTo("redirect:/invoices");
        assertThat(invoiceService.createdInvoiceRequest).isSameAs(requestDTO);
        assertThat(invoiceService.createdByUsername).isEqualTo("lambi");
    }

    @Test
    void createInvoice_shouldCreateInvoiceWithNullUsername_whenCurrentUserIsNull() {
        InvoiceCreateRequestDTO requestDTO = createInvoiceCreateRequestDTO();
        BindingResult bindingResult = new BeanPropertyBindingResult(requestDTO, "invoice");
        Model model = new ExtendedModelMap();

        invoiceController.createInvoice(requestDTO, bindingResult, model, null);

        assertThat(invoiceService.createdByUsername).isNull();
    }

    @Test
    void showEditInvoiceForm_shouldAddFormAttributesAndInvoice() {
        Model model = new ExtendedModelMap();

        String viewName = invoiceController.showEditInvoiceForm(INVOICE_ID, model);

        assertThat(viewName).isEqualTo("invoice-edit");
        assertThat(model.getAttribute("invoice")).isSameAs(invoiceService.invoiceForEdit);
        assertEditFormAttributes(model);
        assertThat(clientService.selectedClientId).isEqualTo(CLIENT_ID);
    }

    @Test
    void editInvoice_shouldReturnEditView_whenBindingResultHasErrors() {
        InvoiceEditRequestDTO requestDTO = createInvoiceEditRequestDTO();
        BindingResult bindingResult = new BeanPropertyBindingResult(requestDTO, "invoice");
        bindingResult.rejectValue("clientId", "client.required", "Client required");
        Model model = new ExtendedModelMap();

        String viewName = invoiceController.editInvoice(INVOICE_ID, requestDTO, bindingResult, model, createUser(UserRole.USER));

        assertThat(viewName).isEqualTo("invoice-edit");
        assertThat(invoiceService.editedInvoiceRequest).isNull();
        assertEditFormAttributes(model);
        assertThat(clientService.selectedClientId).isEqualTo(CLIENT_ID);
    }

    @Test
    void editInvoice_shouldSetIdEditInvoiceAndRedirect_whenRequestIsValid() {
        InvoiceEditRequestDTO requestDTO = createInvoiceEditRequestDTO();
        BindingResult bindingResult = new BeanPropertyBindingResult(requestDTO, "invoice");
        Model model = new ExtendedModelMap();

        String viewName = invoiceController.editInvoice(INVOICE_ID, requestDTO, bindingResult, model, createUser(UserRole.USER));

        assertThat(viewName).isEqualTo("redirect:/invoices");
        assertThat(requestDTO.getId()).isEqualTo(INVOICE_ID);
        assertThat(invoiceService.editedInvoiceRequest).isSameAs(requestDTO);
        assertThat(invoiceService.editedByUsername).isEqualTo("lambi");
    }

    @Test
    void cancelInvoice_shouldRedirectBackWithMessage_whenCurrentUserIsNotAdmin() {
        RedirectAttributesModelMap redirectAttributes = new RedirectAttributesModelMap();

        String viewName = invoiceController.cancelInvoice(INVOICE_ID, createUser(UserRole.USER), redirectAttributes);

        assertThat(viewName).isEqualTo("redirect:/invoices/edit/" + INVOICE_ID);
        assertThat(invoiceService.cancelledInvoiceId).isNull();
        assertThat(redirectAttributes.getFlashAttributes().get("message")).isEqualTo("Only admins can cancel invoices");
    }

    @Test
    void cancelInvoice_shouldCancelInvoiceAndRedirect_whenCurrentUserIsAdmin() {
        RedirectAttributesModelMap redirectAttributes = new RedirectAttributesModelMap();

        String viewName = invoiceController.cancelInvoice(INVOICE_ID, createUser(UserRole.ADMIN), redirectAttributes);

        assertThat(viewName).isEqualTo("redirect:/invoices");
        assertThat(invoiceService.cancelledInvoiceId).isEqualTo(INVOICE_ID);
        assertThat(invoiceService.cancelledByUsername).isEqualTo("lambi");
    }

    @Test
    void restoreInvoice_shouldRedirectBackWithMessage_whenCurrentUserIsNotAdmin() {
        RedirectAttributesModelMap redirectAttributes = new RedirectAttributesModelMap();

        String viewName = invoiceController.restoreInvoice(INVOICE_ID, createUser(UserRole.USER), redirectAttributes);

        assertThat(viewName).isEqualTo("redirect:/invoices/edit/" + INVOICE_ID);
        assertThat(invoiceService.restoredInvoiceId).isNull();
        assertThat(redirectAttributes.getFlashAttributes().get("message")).isEqualTo("Only admins can restore invoices");
    }

    @Test
    void restoreInvoice_shouldRestoreInvoiceAndRedirect_whenCurrentUserIsAdmin() {
        RedirectAttributesModelMap redirectAttributes = new RedirectAttributesModelMap();

        String viewName = invoiceController.restoreInvoice(INVOICE_ID, createUser(UserRole.ADMIN), redirectAttributes);

        assertThat(viewName).isEqualTo("redirect:/invoices");
        assertThat(invoiceService.restoredInvoiceId).isEqualTo(INVOICE_ID);
        assertThat(invoiceService.restoredByUsername).isEqualTo("lambi");
    }

    @Test
    void clearInvoiceHistory_shouldRedirectBackWithMessage_whenCurrentUserIsNotAdmin() {
        RedirectAttributesModelMap redirectAttributes = new RedirectAttributesModelMap();

        String viewName = invoiceController.clearInvoiceHistory(INVOICE_ID, createUser(UserRole.USER), redirectAttributes);

        assertThat(viewName).isEqualTo("redirect:/invoices/" + INVOICE_ID);
        assertThat(invoiceHistoryIntegrationService.clearedHistoryInvoiceId).isNull();
        assertThat(redirectAttributes.getFlashAttributes().get("message")).isEqualTo("Only admins can clear invoice history");
    }

    @Test
    void clearInvoiceHistory_shouldClearHistoryAndRedirectWithMessage_whenCurrentUserIsAdmin() {
        RedirectAttributesModelMap redirectAttributes = new RedirectAttributesModelMap();

        String viewName = invoiceController.clearInvoiceHistory(INVOICE_ID, createUser(UserRole.ADMIN), redirectAttributes);

        assertThat(viewName).isEqualTo("redirect:/invoices/" + INVOICE_ID);
        assertThat(invoiceHistoryIntegrationService.clearedHistoryInvoiceId).isEqualTo(INVOICE_ID);
        assertThat(redirectAttributes.getFlashAttributes().get("message")).isEqualTo("Invoice history cleared successfully");
    }

    @Test
    void clearInvoiceHistory_shouldRedirectWithFailureMessage_whenServiceThrowsException() {
        RedirectAttributesModelMap redirectAttributes = new RedirectAttributesModelMap();
        invoiceHistoryIntegrationService.clearException = new RuntimeException("History service down");

        String viewName = invoiceController.clearInvoiceHistory(INVOICE_ID, createUser(UserRole.ADMIN), redirectAttributes);

        assertThat(viewName).isEqualTo("redirect:/invoices/" + INVOICE_ID);
        assertThat(redirectAttributes.getFlashAttributes().get("message")).isEqualTo("Could not clear invoice history");
    }

    private void assertCreateFormAttributes(Model model) {
        assertThat(model.getAttribute("invoiceTypes")).isEqualTo(InvoiceType.values());
        assertThat(model.getAttribute("invoiceCurrencies")).isEqualTo(InvoiceCurrency.values());
        assertThat(model.getAttribute("measurementUnits")).isEqualTo(MeasurementUnit.values());
        assertThat(model.getAttribute("vatRates")).isEqualTo(VatRate.values());
        assertThat(model.getAttribute("clients")).isEqualTo(clientService.activeClients);
    }

    private void assertEditFormAttributes(Model model) {
        assertThat(model.getAttribute("invoiceTypes")).isEqualTo(InvoiceType.values());
        assertThat(model.getAttribute("invoiceCurrencies")).isEqualTo(InvoiceCurrency.values());
        assertThat(model.getAttribute("measurementUnits")).isEqualTo(MeasurementUnit.values());
        assertThat(model.getAttribute("vatRates")).isEqualTo(VatRate.values());
        assertThat(model.getAttribute("clients")).isEqualTo(clientService.activeClientsWithSelected);
    }

    private InvoiceCreateRequestDTO createInvoiceCreateRequestDTO() {
        return InvoiceCreateRequestDTO.builder()
                .invoiceType(InvoiceType.INVOICE)
                .invoiceSequence(1L)
                .invoiceNumber("0000000001")
                .currency(InvoiceCurrency.BGN)
                .issueDate(LocalDate.of(2026, 8, 2))
                .dueDate(LocalDate.of(2026, 8, 16))
                .clientId(CLIENT_ID)
                .build();
    }

    private InvoiceEditRequestDTO createInvoiceEditRequestDTO() {
        return InvoiceEditRequestDTO.builder()
                .id(INVOICE_ID)
                .invoiceType(InvoiceType.INVOICE)
                .invoiceNumber("0000000001")
                .currency(InvoiceCurrency.BGN)
                .status(InvoiceStatus.ISSUED)
                .issueDate(LocalDate.of(2026, 8, 2))
                .dueDate(LocalDate.of(2026, 8, 16))
                .clientId(CLIENT_ID)
                .build();
    }

    private AuthenticatedUserDetails createUser(UserRole role) {
        return new AuthenticatedUserDetails(UUID.randomUUID(), "lambi", "password", role, true);
    }

    private static final class FakeInvoiceService implements InvoiceService {

        private final List<InvoiceShowAllDTO> invoices = List.of(InvoiceShowAllDTO.builder()
                .id(INVOICE_ID)
                .invoiceNumber("0000000001")
                .build());
        private final InvoiceCreateRequestDTO preparedInvoice = InvoiceCreateRequestDTO.builder()
                .invoiceNumber("0000000001")
                .build();
        private final InvoiceDetailsDTO invoiceDetails = InvoiceDetailsDTO.builder()
                .id(INVOICE_ID)
                .invoiceNumber("0000000001")
                .build();
        private final InvoiceEditRequestDTO invoiceForEdit = InvoiceEditRequestDTO.builder()
                .id(INVOICE_ID)
                .clientId(CLIENT_ID)
                .invoiceNumber("0000000001")
                .build();

        private String lastCompanyName;
        private UUID lastFindInvoiceId;
        private InvoiceCreateRequestDTO createdInvoiceRequest;
        private String createdByUsername;
        private InvoiceEditRequestDTO editedInvoiceRequest;
        private String editedByUsername;
        private UUID cancelledInvoiceId;
        private String cancelledByUsername;
        private UUID restoredInvoiceId;
        private String restoredByUsername;

        @Override
        public List<InvoiceShowAllDTO> findAllInvoices() {
            return invoices;
        }

        @Override
        public List<InvoiceShowAllDTO> findInvoicesByCompanyName(String companyName) {
            lastCompanyName = companyName;
            return invoices;
        }

        @Override
        public InvoiceDetailsDTO findInvoiceById(UUID invoiceId) {
            lastFindInvoiceId = invoiceId;
            return invoiceDetails;
        }

        @Override
        public InvoiceCreateRequestDTO prepareCreateInvoiceForm() {
            return preparedInvoice;
        }

        @Override
        public Invoice createInvoice(InvoiceCreateRequestDTO invoiceCreateRequestDTO, String performedByUsername) {
            createdInvoiceRequest = invoiceCreateRequestDTO;
            createdByUsername = performedByUsername;
            return Invoice.builder().build();
        }

        @Override
        public InvoiceEditRequestDTO getInvoiceForEdit(UUID invoiceId) {
            return invoiceForEdit;
        }

        @Override
        public void editInvoice(InvoiceEditRequestDTO invoiceEditRequestDTO, String performedByUsername) {
            editedInvoiceRequest = invoiceEditRequestDTO;
            editedByUsername = performedByUsername;
        }

        @Override
        public void cancelInvoice(UUID invoiceId, String performedByUsername) {
            cancelledInvoiceId = invoiceId;
            cancelledByUsername = performedByUsername;
        }

        @Override
        public void restoreInvoice(UUID invoiceId, String performedByUsername) {
            restoredInvoiceId = invoiceId;
            restoredByUsername = performedByUsername;
        }

        @Override
        public int markOverdueInvoices() {
            return 0;
        }

        @Override
        public int markNoLongerOverdueInvoices() {
            return 0;
        }
    }

    private static final class FakeClientService implements ClientService {

        private final List<ClientSelectDTO> activeClients = List.of(ClientSelectDTO.builder()
                .id(CLIENT_ID)
                .displayName("Lambi")
                .build());
        private final List<ClientSelectDTO> activeClientsWithSelected = List.of(ClientSelectDTO.builder()
                .id(CLIENT_ID)
                .displayName("Lambi selected")
                .build());

        private UUID selectedClientId;

        @Override
        public Client createClient(bg.softuni.invoiceapplication.model.dto.clients.ClientCreateRequestDTO clientCreateRequestDTO) {
            return null;
        }

        @Override
        public List<ClientShowAllDTO> findAllClients() {
            return List.of();
        }

        @Override
        public List<ClientSelectDTO> findAllActiveClientsForSelect() {
            return activeClients;
        }

        @Override
        public List<ClientSelectDTO> findAllActiveClientsForSelect(UUID selectedClientId) {
            this.selectedClientId = selectedClientId;
            return activeClientsWithSelected;
        }

        @Override
        public List<ClientShowAllDTO> findClientsByName(String clientName) {
            return List.of();
        }

        @Override
        public bg.softuni.invoiceapplication.model.dto.clients.ClientEditRequestDTO getClientForEdit(UUID id) {
            return null;
        }

        @Override
        public Optional<String> findDuplicateFieldForEdit(bg.softuni.invoiceapplication.model.dto.clients.ClientEditRequestDTO clientEditRequestDTO) {
            return Optional.empty();
        }

        @Override
        public void editClient(bg.softuni.invoiceapplication.model.dto.clients.ClientEditRequestDTO clientEditRequestDTO) {
        }

        @Override
        public void toggleClientActive(UUID id) {
        }

        @Override
        public void deleteClient(UUID id) {
        }
    }

    private static final class FakeInvoiceHistoryIntegrationService implements InvoiceHistoryIntegrationService {

        private final List<InvoiceHistoryResponseDTO> history = List.of(InvoiceHistoryResponseDTO.builder()
                .invoiceId(INVOICE_ID)
                .revisionNumber(1)
                .build());

        private UUID lastFindHistoryInvoiceId;
        private UUID clearedHistoryInvoiceId;
        private RuntimeException clearException;

        @Override
        public InvoiceHistoryResponseDTO createHistoryRecord(InvoiceHistoryCreateRequestDTO invoiceHistoryCreateRequestDTO) {
            return null;
        }

        @Override
        public List<InvoiceHistoryResponseDTO> findHistoryByInvoiceId(UUID invoiceId) {
            lastFindHistoryInvoiceId = invoiceId;
            return history;
        }

        @Override
        public void clearHistoryByInvoiceId(UUID invoiceId) {
            if (clearException != null) {
                throw clearException;
            }
            clearedHistoryInvoiceId = invoiceId;
        }
    }
}
