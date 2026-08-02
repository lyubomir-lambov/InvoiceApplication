package bg.softuni.invoiceapplication.web;

import bg.softuni.invoiceapplication.model.dto.users.UserProfileDTO;
import bg.softuni.invoiceapplication.model.dto.users.UserProfileEditRequestDTO;
import bg.softuni.invoiceapplication.model.dto.users.UserRegistrationRequestDTO;
import bg.softuni.invoiceapplication.model.dto.users.UserRegistrationResponseDTO;
import bg.softuni.invoiceapplication.model.dto.users.UserShowAllDTO;
import bg.softuni.invoiceapplication.model.enums.UserRole;
import bg.softuni.invoiceapplication.security.AuthenticatedUserDetails;
import bg.softuni.invoiceapplication.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class UserControllerTest {

    private static final UUID ADMIN_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID USER_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");

    private FakeUserService userService;
    private UserController userController;

    @BeforeEach
    void setUp() {
        userService = new FakeUserService();
        userController = new UserController(userService);
    }

    @Test
    void users_shouldRedirectToInvoices_whenCurrentUserIsNull() {
        Model model = new ExtendedModelMap();

        String viewName = userController.users("lambi", null, model);

        assertThat(viewName).isEqualTo("redirect:/invoices");
        assertThat(userService.lastSearchedUsername).isNull();
    }

    @Test
    void users_shouldRedirectToInvoices_whenCurrentUserIsNotAdmin() {
        Model model = new ExtendedModelMap();

        String viewName = userController.users("lambi", createUser(USER_ID, UserRole.USER), model);

        assertThat(viewName).isEqualTo("redirect:/invoices");
        assertThat(userService.lastSearchedUsername).isNull();
    }

    @Test
    void users_shouldAddUsersAndSearchedUsernameToModel_whenCurrentUserIsAdmin() {
        Model model = new ExtendedModelMap();

        String viewName = userController.users("lambi", createUser(ADMIN_ID, UserRole.ADMIN), model);

        assertThat(viewName).isEqualTo("users");
        assertThat(model.getAttribute("users")).isEqualTo(userService.users);
        assertThat(model.getAttribute("searchedUsername")).isEqualTo("lambi");
        assertThat(userService.lastSearchedUsername).isEqualTo("lambi");
    }

    @Test
    void registerForm_shouldAddUserToModelAndReturnRegisterView() {
        Model model = new ExtendedModelMap();

        String viewName = userController.registerForm(model);

        assertThat(viewName).isEqualTo("user-register");
        assertThat(model.getAttribute("user")).isInstanceOf(UserRegistrationRequestDTO.class);
    }

    @Test
    void registerUser_shouldReturnRegisterView_whenBindingResultHasErrors() {
        UserRegistrationRequestDTO requestDTO = createRegistrationRequestDTO();
        BindingResult bindingResult = new BeanPropertyBindingResult(requestDTO, "user");
        bindingResult.rejectValue("username", "username.invalid", "Invalid username");
        RedirectAttributesModelMap redirectAttributes = new RedirectAttributesModelMap();

        String viewName = userController.registerUser(requestDTO, bindingResult, redirectAttributes);

        assertThat(viewName).isEqualTo("user-register");
        assertThat(userService.registrationRequest).isNull();
        assertThat(redirectAttributes.getFlashAttributes()).isEmpty();
    }

    @Test
    void registerUser_shouldRegisterUserAndRedirectToSuccess_whenRequestIsValid() {
        UserRegistrationRequestDTO requestDTO = createRegistrationRequestDTO();
        BindingResult bindingResult = new BeanPropertyBindingResult(requestDTO, "user");
        RedirectAttributesModelMap redirectAttributes = new RedirectAttributesModelMap();

        String viewName = userController.registerUser(requestDTO, bindingResult, redirectAttributes);

        assertThat(viewName).isEqualTo("redirect:/users/register/success");
        assertThat(userService.registrationRequest).isSameAs(requestDTO);
        assertThat(redirectAttributes.getFlashAttributes().get("user")).isEqualTo(userService.registrationResponse);
    }

    @Test
    void toggleUserStatus_shouldRedirectToInvoicesWithMessage_whenCurrentUserIsNull() {
        RedirectAttributesModelMap redirectAttributes = new RedirectAttributesModelMap();

        String viewName = userController.toggleUserStatus(USER_ID, null, redirectAttributes);

        assertThat(viewName).isEqualTo("redirect:/invoices");
        assertThat(userService.toggledStatusUserId).isNull();
        assertThat(redirectAttributes.getFlashAttributes().get("message")).isEqualTo("Only admins can change user status");
    }

    @Test
    void toggleUserStatus_shouldRedirectToInvoicesWithMessage_whenCurrentUserIsNotAdmin() {
        RedirectAttributesModelMap redirectAttributes = new RedirectAttributesModelMap();

        String viewName = userController.toggleUserStatus(USER_ID, createUser(USER_ID, UserRole.USER), redirectAttributes);

        assertThat(viewName).isEqualTo("redirect:/invoices");
        assertThat(userService.toggledStatusUserId).isNull();
        assertThat(redirectAttributes.getFlashAttributes().get("message")).isEqualTo("Only admins can change user status");
    }

    @Test
    void toggleUserStatus_shouldRedirectToUsersWithMessage_whenAdminTriesToChangeOwnStatus() {
        RedirectAttributesModelMap redirectAttributes = new RedirectAttributesModelMap();

        String viewName = userController.toggleUserStatus(ADMIN_ID, createUser(ADMIN_ID, UserRole.ADMIN), redirectAttributes);

        assertThat(viewName).isEqualTo("redirect:/users");
        assertThat(userService.toggledStatusUserId).isNull();
        assertThat(redirectAttributes.getFlashAttributes().get("message")).isEqualTo("You cannot change your own status");
    }

    @Test
    void toggleUserStatus_shouldToggleStatusAndRedirect_whenCurrentUserIsAdmin() {
        RedirectAttributesModelMap redirectAttributes = new RedirectAttributesModelMap();

        String viewName = userController.toggleUserStatus(USER_ID, createUser(ADMIN_ID, UserRole.ADMIN), redirectAttributes);

        assertThat(viewName).isEqualTo("redirect:/users");
        assertThat(userService.toggledStatusUserId).isEqualTo(USER_ID);
    }

    @Test
    void toggleUserRole_shouldRedirectToInvoicesWithMessage_whenCurrentUserIsNull() {
        RedirectAttributesModelMap redirectAttributes = new RedirectAttributesModelMap();

        String viewName = userController.toggleUserRole(USER_ID, null, redirectAttributes);

        assertThat(viewName).isEqualTo("redirect:/invoices");
        assertThat(userService.toggledRoleUserId).isNull();
        assertThat(redirectAttributes.getFlashAttributes().get("message")).isEqualTo("Only admins can change user roles");
    }

    @Test
    void toggleUserRole_shouldRedirectToInvoicesWithMessage_whenCurrentUserIsNotAdmin() {
        RedirectAttributesModelMap redirectAttributes = new RedirectAttributesModelMap();

        String viewName = userController.toggleUserRole(USER_ID, createUser(USER_ID, UserRole.USER), redirectAttributes);

        assertThat(viewName).isEqualTo("redirect:/invoices");
        assertThat(userService.toggledRoleUserId).isNull();
        assertThat(redirectAttributes.getFlashAttributes().get("message")).isEqualTo("Only admins can change user roles");
    }

    @Test
    void toggleUserRole_shouldRedirectToUsersWithMessage_whenAdminTriesToChangeOwnRole() {
        RedirectAttributesModelMap redirectAttributes = new RedirectAttributesModelMap();

        String viewName = userController.toggleUserRole(ADMIN_ID, createUser(ADMIN_ID, UserRole.ADMIN), redirectAttributes);

        assertThat(viewName).isEqualTo("redirect:/users");
        assertThat(userService.toggledRoleUserId).isNull();
        assertThat(redirectAttributes.getFlashAttributes().get("message")).isEqualTo("You cannot change your own role");
    }

    @Test
    void toggleUserRole_shouldToggleRoleAndRedirect_whenCurrentUserIsAdmin() {
        RedirectAttributesModelMap redirectAttributes = new RedirectAttributesModelMap();

        String viewName = userController.toggleUserRole(USER_ID, createUser(ADMIN_ID, UserRole.ADMIN), redirectAttributes);

        assertThat(viewName).isEqualTo("redirect:/users");
        assertThat(userService.toggledRoleUserId).isEqualTo(USER_ID);
    }

    @Test
    void registerSuccess_shouldReturnRegisterSuccessView() {
        String viewName = userController.registerSuccess();

        assertThat(viewName).isEqualTo("user-register-success");
    }

    private UserRegistrationRequestDTO createRegistrationRequestDTO() {
        return UserRegistrationRequestDTO.builder()
                .username("newuser")
                .password("password")
                .passwordConfirm("password")
                .email("newuser@example.com")
                .build();
    }

    private AuthenticatedUserDetails createUser(UUID id, UserRole role) {
        return new AuthenticatedUserDetails(id, "user", "password", role, true);
    }

    private static final class FakeUserService implements UserService {

        private final List<UserShowAllDTO> users = List.of(UserShowAllDTO.builder()
                .id(USER_ID)
                .username("lambi")
                .email("lambi@example.com")
                .role(UserRole.USER)
                .active(true)
                .createdOn(LocalDateTime.of(2026, 8, 2, 12, 30))
                .build());
        private final UserRegistrationResponseDTO registrationResponse = UserRegistrationResponseDTO.builder()
                .username("newuser")
                .email("newuser@example.com")
                .createdOn(LocalDateTime.of(2026, 8, 2, 12, 30))
                .build();

        private String lastSearchedUsername;
        private UserRegistrationRequestDTO registrationRequest;
        private UUID toggledStatusUserId;
        private UUID toggledRoleUserId;

        @Override
        public UserRegistrationResponseDTO registerUser(UserRegistrationRequestDTO userRegistrationRequestDTO) {
            registrationRequest = userRegistrationRequestDTO;
            return registrationResponse;
        }

        @Override
        public List<UserShowAllDTO> findAllUsers() {
            return users;
        }

        @Override
        public List<UserShowAllDTO> findUsersByUsername(String username) {
            lastSearchedUsername = username;
            return users;
        }

        @Override
        public UserProfileDTO findUserProfile(UUID userId) {
            return null;
        }

        @Override
        public UserProfileEditRequestDTO getUserProfileForEdit(UUID userId) {
            return null;
        }

        @Override
        public void editUserProfile(UUID userId, UserProfileEditRequestDTO userProfileEditRequestDTO) {
        }

        @Override
        public void toggleUserStatus(UUID userId) {
            toggledStatusUserId = userId;
        }

        @Override
        public void toggleUserRole(UUID userId) {
            toggledRoleUserId = userId;
        }
    }
}
