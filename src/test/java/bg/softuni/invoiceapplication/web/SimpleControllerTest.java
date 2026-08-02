package bg.softuni.invoiceapplication.web;

import bg.softuni.invoiceapplication.exception.BusinessRuleException;
import bg.softuni.invoiceapplication.model.dto.clients.ClientSelectDTO;
import bg.softuni.invoiceapplication.model.dto.reports.PaymentReportByCurrencyDTO;
import bg.softuni.invoiceapplication.model.dto.reports.PaymentReportClientDTO;
import bg.softuni.invoiceapplication.model.dto.users.UserLoginRequestDTO;
import bg.softuni.invoiceapplication.model.dto.users.UserProfileDTO;
import bg.softuni.invoiceapplication.model.dto.users.UserProfileEditRequestDTO;
import bg.softuni.invoiceapplication.model.dto.users.UserRegistrationRequestDTO;
import bg.softuni.invoiceapplication.model.dto.users.UserRegistrationResponseDTO;
import bg.softuni.invoiceapplication.model.dto.users.UserShowAllDTO;
import bg.softuni.invoiceapplication.model.enums.InvoiceCurrency;
import bg.softuni.invoiceapplication.model.enums.UserRole;
import bg.softuni.invoiceapplication.security.AuthenticatedUserDetails;
import bg.softuni.invoiceapplication.service.ClientService;
import bg.softuni.invoiceapplication.service.PaymentReportService;
import bg.softuni.invoiceapplication.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class SimpleControllerTest {

    private static final UUID USER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID CLIENT_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");

    @Test
    void homeIndex_shouldReturnIndex_whenCurrentUserIsNull() {
        HomeController homeController = new HomeController();

        String viewName = homeController.index(null);

        assertThat(viewName).isEqualTo("index");
    }

    @Test
    void homeIndex_shouldRedirectToInvoices_whenCurrentUserExists() {
        HomeController homeController = new HomeController();

        String viewName = homeController.index(createUser());

        assertThat(viewName).isEqualTo("redirect:/invoices");
    }

    @Test
    void loginForm_shouldAddUserToModelAndReturnLoginView() {
        AuthController authController = new AuthController();
        Model model = new ExtendedModelMap();

        String viewName = authController.loginForm(model);

        assertThat(viewName).isEqualTo("user-login");
        assertThat(model.getAttribute("user")).isInstanceOf(UserLoginRequestDTO.class);
    }

    @Test
    void showPaymentReports_shouldAddReportAttributesToModel() {
        FakeClientService clientService = new FakeClientService();
        FakePaymentReportService paymentReportService = new FakePaymentReportService();
        PaymentReportController paymentReportController = new PaymentReportController(paymentReportService, clientService);
        Model model = new ExtendedModelMap();

        String viewName = paymentReportController.showPaymentReports(CLIENT_ID, model);

        assertThat(viewName).isEqualTo("payment-reports");
        assertThat(model.getAttribute("clients")).isEqualTo(clientService.activeClients);
        assertThat(model.getAttribute("selectedClientId")).isEqualTo(CLIENT_ID);
        assertThat(model.getAttribute("reports")).isEqualTo(paymentReportService.currencyReports);
        assertThat(model.getAttribute("clientReports")).isEqualTo(paymentReportService.clientReports);
        assertThat(paymentReportService.lastCurrencyReportClientId).isEqualTo(CLIENT_ID);
        assertThat(paymentReportService.lastClientReportClientId).isEqualTo(CLIENT_ID);
    }

    @Test
    void showProfile_shouldAddProfileToModel() {
        FakeUserService userService = new FakeUserService();
        ProfileController profileController = new ProfileController(userService);
        Model model = new ExtendedModelMap();

        String viewName = profileController.showProfile(createUser(), model);

        assertThat(viewName).isEqualTo("profile");
        assertThat(model.getAttribute("profile")).isEqualTo(userService.profileDTO);
        assertThat(userService.lastProfileUserId).isEqualTo(USER_ID);
    }

    @Test
    void showEditProfileForm_shouldAddProfileEditDTOToModel() {
        FakeUserService userService = new FakeUserService();
        ProfileController profileController = new ProfileController(userService);
        Model model = new ExtendedModelMap();

        String viewName = profileController.showEditProfileForm(createUser(), model);

        assertThat(viewName).isEqualTo("profile-edit");
        assertThat(model.getAttribute("profile")).isEqualTo(userService.profileEditRequestDTO);
        assertThat(userService.lastProfileEditUserId).isEqualTo(USER_ID);
    }

    @Test
    void editProfile_shouldReturnProfileEditView_whenBindingResultHasErrors() {
        FakeUserService userService = new FakeUserService();
        ProfileController profileController = new ProfileController(userService);
        UserProfileEditRequestDTO requestDTO = createProfileEditRequestDTO();
        BindingResult bindingResult = new BeanPropertyBindingResult(requestDTO, "profile");
        bindingResult.rejectValue("email", "email.invalid", "Invalid email");
        RedirectAttributesModelMap redirectAttributes = new RedirectAttributesModelMap();

        String viewName = profileController.editProfile(createUser(), requestDTO, bindingResult, redirectAttributes);

        assertThat(viewName).isEqualTo("profile-edit");
        assertThat(userService.editedProfileRequestDTO).isNull();
    }

    @Test
    void editProfile_shouldReturnProfileEditViewAndRejectEmail_whenBusinessRuleExceptionIsThrown() {
        FakeUserService userService = new FakeUserService();
        userService.editException = new BusinessRuleException("Email is already in use");
        ProfileController profileController = new ProfileController(userService);
        UserProfileEditRequestDTO requestDTO = createProfileEditRequestDTO();
        BindingResult bindingResult = new BeanPropertyBindingResult(requestDTO, "profile");
        RedirectAttributesModelMap redirectAttributes = new RedirectAttributesModelMap();

        String viewName = profileController.editProfile(createUser(), requestDTO, bindingResult, redirectAttributes);

        assertThat(viewName).isEqualTo("profile-edit");
        assertThat(bindingResult.getFieldError("email")).isNotNull();
        assertThat(bindingResult.getFieldError("email").getDefaultMessage()).isEqualTo("Email is already in use");
    }

    @Test
    void editProfile_shouldEditProfileAndRedirect_whenRequestIsValid() {
        FakeUserService userService = new FakeUserService();
        ProfileController profileController = new ProfileController(userService);
        UserProfileEditRequestDTO requestDTO = createProfileEditRequestDTO();
        BindingResult bindingResult = new BeanPropertyBindingResult(requestDTO, "profile");
        RedirectAttributesModelMap redirectAttributes = new RedirectAttributesModelMap();

        String viewName = profileController.editProfile(createUser(), requestDTO, bindingResult, redirectAttributes);

        assertThat(viewName).isEqualTo("redirect:/profile");
        assertThat(userService.editedProfileUserId).isEqualTo(USER_ID);
        assertThat(userService.editedProfileRequestDTO).isSameAs(requestDTO);
        assertThat(redirectAttributes.getFlashAttributes().get("message")).isEqualTo("Profile updated successfully");
    }

    private static AuthenticatedUserDetails createUser() {
        return new AuthenticatedUserDetails(USER_ID, "lambi", "password", UserRole.ADMIN, true);
    }

    private static UserProfileEditRequestDTO createProfileEditRequestDTO() {
        return UserProfileEditRequestDTO.builder()
                .email("updated@example.com")
                .firstName("Updated")
                .lastName("User")
                .phoneNumber("+359899999999")
                .address("Plovdiv")
                .build();
    }

    private static final class FakePaymentReportService implements PaymentReportService {

        private final List<PaymentReportByCurrencyDTO> currencyReports = List.of(PaymentReportByCurrencyDTO.builder()
                .currency(InvoiceCurrency.BGN)
                .invoiceTotal(new BigDecimal("100.00"))
                .paymentTotal(new BigDecimal("50.00"))
                .dueAmount(new BigDecimal("50.00"))
                .build());
        private final List<PaymentReportClientDTO> clientReports = List.of(PaymentReportClientDTO.builder()
                .clientId(CLIENT_ID)
                .clientDisplayName("Lambi")
                .build());

        private UUID lastCurrencyReportClientId;
        private UUID lastClientReportClientId;

        @Override
        public List<PaymentReportByCurrencyDTO> getReportsByCurrency(UUID clientId) {
            lastCurrencyReportClientId = clientId;
            return currencyReports;
        }

        @Override
        public List<PaymentReportClientDTO> getClientReports(UUID clientId) {
            lastClientReportClientId = clientId;
            return clientReports;
        }
    }

    private static final class FakeClientService implements ClientService {

        private final List<ClientSelectDTO> activeClients = List.of(ClientSelectDTO.builder()
                .id(CLIENT_ID)
                .displayName("Lambi")
                .build());

        @Override
        public bg.softuni.invoiceapplication.model.entity.Client createClient(bg.softuni.invoiceapplication.model.dto.clients.ClientCreateRequestDTO clientCreateRequestDTO) {
            return null;
        }

        @Override
        public List<bg.softuni.invoiceapplication.model.dto.clients.ClientShowAllDTO> findAllClients() {
            return List.of();
        }

        @Override
        public List<ClientSelectDTO> findAllActiveClientsForSelect() {
            return activeClients;
        }

        @Override
        public List<ClientSelectDTO> findAllActiveClientsForSelect(UUID selectedClientId) {
            return activeClients;
        }

        @Override
        public List<bg.softuni.invoiceapplication.model.dto.clients.ClientShowAllDTO> findClientsByName(String clientName) {
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

    private static final class FakeUserService implements UserService {

        private final UserProfileDTO profileDTO = UserProfileDTO.builder()
                .id(USER_ID)
                .username("lambi")
                .email("lambi@example.com")
                .build();
        private final UserProfileEditRequestDTO profileEditRequestDTO = UserProfileEditRequestDTO.builder()
                .email("lambi@example.com")
                .build();

        private UUID lastProfileUserId;
        private UUID lastProfileEditUserId;
        private UUID editedProfileUserId;
        private UserProfileEditRequestDTO editedProfileRequestDTO;
        private BusinessRuleException editException;

        @Override
        public UserRegistrationResponseDTO registerUser(UserRegistrationRequestDTO userRegistrationRequestDTO) {
            return null;
        }

        @Override
        public List<UserShowAllDTO> findAllUsers() {
            return List.of();
        }

        @Override
        public List<UserShowAllDTO> findUsersByUsername(String username) {
            return List.of();
        }

        @Override
        public UserProfileDTO findUserProfile(UUID userId) {
            lastProfileUserId = userId;
            return profileDTO;
        }

        @Override
        public UserProfileEditRequestDTO getUserProfileForEdit(UUID userId) {
            lastProfileEditUserId = userId;
            return profileEditRequestDTO;
        }

        @Override
        public void editUserProfile(UUID userId, UserProfileEditRequestDTO userProfileEditRequestDTO) {
            if (editException != null) {
                throw editException;
            }
            editedProfileUserId = userId;
            editedProfileRequestDTO = userProfileEditRequestDTO;
        }

        @Override
        public void toggleUserStatus(UUID userId) {
        }

        @Override
        public void toggleUserRole(UUID userId) {
        }
    }
}
