package bg.softuni.invoiceapplication.web;

import bg.softuni.invoiceapplication.model.dto.clients.ClientSelectDTO;
import bg.softuni.invoiceapplication.model.dto.clients.ClientShowAllDTO;
import bg.softuni.invoiceapplication.model.dto.payments.PaymentCreateRequestDTO;
import bg.softuni.invoiceapplication.model.dto.payments.PaymentEditRequestDTO;
import bg.softuni.invoiceapplication.model.entity.Client;
import bg.softuni.invoiceapplication.model.entity.Payment;
import bg.softuni.invoiceapplication.model.enums.InvoiceCurrency;
import bg.softuni.invoiceapplication.model.enums.UserRole;
import bg.softuni.invoiceapplication.security.AuthenticatedUserDetails;
import bg.softuni.invoiceapplication.service.ClientService;
import bg.softuni.invoiceapplication.service.PaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PaymentControllerTest {

    private static final UUID PAYMENT_ID = UUID.fromString("44444444-4444-4444-4444-444444444444");
    private static final UUID CLIENT_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");

    private FakePaymentService paymentService;
    private FakeClientService clientService;
    private PaymentController paymentController;

    @BeforeEach
    void setUp() {
        paymentService = new FakePaymentService();
        clientService = new FakeClientService();
        paymentController = new PaymentController(paymentService, clientService);
    }

    @Test
    void showAllPayments_shouldAddPaymentsAndCompanyNameToModel() {
        Model model = new ExtendedModelMap();

        String viewName = paymentController.showAllPayments("Lambi", model);

        assertThat(viewName).isEqualTo("payments");
        assertThat(model.getAttribute("payments")).isEqualTo(paymentService.payments);
        assertThat(model.getAttribute("companyName")).isEqualTo("Lambi");
        assertThat(paymentService.lastCompanyName).isEqualTo("Lambi");
    }

    @Test
    void showCreatePaymentForm_shouldAddFormAttributes() {
        Model model = new ExtendedModelMap();

        String viewName = paymentController.showCreatePaymentForm(model);

        assertThat(viewName).isEqualTo("payment-create");
        assertThat(model.getAttribute("payment")).isInstanceOf(PaymentCreateRequestDTO.class);
        assertThat(model.getAttribute("clients")).isEqualTo(clientService.activeClients);
        assertThat(model.getAttribute("invoiceCurrencies")).isEqualTo(InvoiceCurrency.values());
    }

    @Test
    void createPayment_shouldReturnCreateView_whenBindingResultHasErrors() {
        PaymentCreateRequestDTO requestDTO = createPaymentCreateRequestDTO();
        BindingResult bindingResult = new BeanPropertyBindingResult(requestDTO, "payment");
        bindingResult.rejectValue("amount", "amount.invalid", "Invalid amount");
        Model model = new ExtendedModelMap();

        String viewName = paymentController.createPayment(requestDTO, bindingResult, model);

        assertThat(viewName).isEqualTo("payment-create");
        assertThat(paymentService.createdPaymentRequest).isNull();
        assertThat(model.getAttribute("clients")).isEqualTo(clientService.activeClients);
        assertThat(model.getAttribute("invoiceCurrencies")).isEqualTo(InvoiceCurrency.values());
    }

    @Test
    void createPayment_shouldCreatePaymentAndRedirect_whenRequestIsValid() {
        PaymentCreateRequestDTO requestDTO = createPaymentCreateRequestDTO();
        BindingResult bindingResult = new BeanPropertyBindingResult(requestDTO, "payment");
        Model model = new ExtendedModelMap();

        String viewName = paymentController.createPayment(requestDTO, bindingResult, model);

        assertThat(viewName).isEqualTo("redirect:/payments");
        assertThat(paymentService.createdPaymentRequest).isSameAs(requestDTO);
    }

    @Test
    void showEditPaymentForm_shouldAddPaymentAndFormAttributes() {
        Model model = new ExtendedModelMap();

        String viewName = paymentController.showEditPaymentForm(PAYMENT_ID, model);

        assertThat(viewName).isEqualTo("payment-edit");
        assertThat(model.getAttribute("payment")).isSameAs(paymentService.paymentForEdit);
        assertThat(model.getAttribute("clients")).isEqualTo(clientService.activeClientsWithSelected);
        assertThat(model.getAttribute("invoiceCurrencies")).isEqualTo(InvoiceCurrency.values());
        assertThat(clientService.selectedClientId).isEqualTo(CLIENT_ID);
    }

    @Test
    void editPayment_shouldReturnEditView_whenBindingResultHasErrors() {
        PaymentEditRequestDTO requestDTO = createPaymentEditRequestDTO();
        BindingResult bindingResult = new BeanPropertyBindingResult(requestDTO, "payment");
        bindingResult.rejectValue("amount", "amount.invalid", "Invalid amount");
        Model model = new ExtendedModelMap();

        String viewName = paymentController.editPayment(PAYMENT_ID, requestDTO, bindingResult, model);

        assertThat(viewName).isEqualTo("payment-edit");
        assertThat(paymentService.editedPaymentRequest).isNull();
        assertThat(model.getAttribute("clients")).isEqualTo(clientService.activeClientsWithSelected);
        assertThat(model.getAttribute("invoiceCurrencies")).isEqualTo(InvoiceCurrency.values());
    }

    @Test
    void editPayment_shouldSetPaymentIdEditPaymentAndRedirect_whenRequestIsValid() {
        PaymentEditRequestDTO requestDTO = createPaymentEditRequestDTO();
        BindingResult bindingResult = new BeanPropertyBindingResult(requestDTO, "payment");
        Model model = new ExtendedModelMap();

        String viewName = paymentController.editPayment(PAYMENT_ID, requestDTO, bindingResult, model);

        assertThat(viewName).isEqualTo("redirect:/payments");
        assertThat(requestDTO.getId()).isEqualTo(PAYMENT_ID);
        assertThat(paymentService.editedPaymentRequest).isSameAs(requestDTO);
    }

    @Test
    void deletePayment_shouldRedirectWithMessage_whenCurrentUserIsNotAdmin() {
        RedirectAttributesModelMap redirectAttributes = new RedirectAttributesModelMap();
        AuthenticatedUserDetails user = createUser(UserRole.USER);

        String viewName = paymentController.deletePayment(PAYMENT_ID, user, redirectAttributes);

        assertThat(viewName).isEqualTo("redirect:/payments");
        assertThat(paymentService.deletedPaymentId).isNull();
        assertThat(redirectAttributes.getFlashAttributes().get("message")).isEqualTo("Only admins can delete payments");
    }

    @Test
    void deletePayment_shouldDeletePaymentAndRedirectWithMessage_whenCurrentUserIsAdmin() {
        RedirectAttributesModelMap redirectAttributes = new RedirectAttributesModelMap();
        AuthenticatedUserDetails admin = createUser(UserRole.ADMIN);

        String viewName = paymentController.deletePayment(PAYMENT_ID, admin, redirectAttributes);

        assertThat(viewName).isEqualTo("redirect:/payments");
        assertThat(paymentService.deletedPaymentId).isEqualTo(PAYMENT_ID);
        assertThat(redirectAttributes.getFlashAttributes().get("message")).isEqualTo("Payment deleted successfully");
    }

    @Test
    void deletePayment_shouldRedirectWithExceptionMessage_whenServiceThrowsException() {
        RedirectAttributesModelMap redirectAttributes = new RedirectAttributesModelMap();
        AuthenticatedUserDetails admin = createUser(UserRole.ADMIN);
        paymentService.deleteException = new RuntimeException("Delete failed");

        String viewName = paymentController.deletePayment(PAYMENT_ID, admin, redirectAttributes);

        assertThat(viewName).isEqualTo("redirect:/payments");
        assertThat(redirectAttributes.getFlashAttributes().get("message")).isEqualTo("Delete failed");
    }

    private PaymentCreateRequestDTO createPaymentCreateRequestDTO() {
        return PaymentCreateRequestDTO.builder()
                .clientId(CLIENT_ID)
                .amount(new BigDecimal("150.00"))
                .currency(InvoiceCurrency.BGN)
                .paymentDate(LocalDate.of(2026, 8, 2))
                .notes("Payment")
                .build();
    }

    private PaymentEditRequestDTO createPaymentEditRequestDTO() {
        return PaymentEditRequestDTO.builder()
                .id(PAYMENT_ID)
                .clientId(CLIENT_ID)
                .amount(new BigDecimal("150.00"))
                .currency(InvoiceCurrency.BGN)
                .paymentDate(LocalDate.of(2026, 8, 2))
                .notes("Payment")
                .build();
    }

    private AuthenticatedUserDetails createUser(UserRole role) {
        return new AuthenticatedUserDetails(UUID.randomUUID(), "user", "password", role, true);
    }

    private static final class FakePaymentService implements PaymentService {

        private final List<Payment> payments = List.of(Payment.builder().build());
        private final PaymentEditRequestDTO paymentForEdit = PaymentEditRequestDTO.builder()
                .id(PAYMENT_ID)
                .clientId(CLIENT_ID)
                .amount(new BigDecimal("150.00"))
                .currency(InvoiceCurrency.BGN)
                .paymentDate(LocalDate.of(2026, 8, 2))
                .build();

        private String lastCompanyName;
        private PaymentCreateRequestDTO createdPaymentRequest;
        private PaymentEditRequestDTO editedPaymentRequest;
        private UUID deletedPaymentId;
        private RuntimeException deleteException;

        @Override
        public Payment createPayment(PaymentCreateRequestDTO paymentCreateRequestDTO) {
            createdPaymentRequest = paymentCreateRequestDTO;
            return Payment.builder().build();
        }

        @Override
        public List<Payment> findAllPayments() {
            return payments;
        }

        @Override
        public List<Payment> findPaymentsByCompanyName(String companyName) {
            lastCompanyName = companyName;
            return payments;
        }

        @Override
        public PaymentEditRequestDTO getPaymentForEdit(UUID paymentId) {
            return paymentForEdit;
        }

        @Override
        public void editPayment(PaymentEditRequestDTO paymentEditRequestDTO) {
            editedPaymentRequest = paymentEditRequestDTO;
        }

        @Override
        public void deletePayment(UUID paymentId) {
            if (deleteException != null) {
                throw deleteException;
            }
            deletedPaymentId = paymentId;
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
}
