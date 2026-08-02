package bg.softuni.invoiceapplication.service.impl;

import bg.softuni.invoiceapplication.exception.BusinessRuleException;
import bg.softuni.invoiceapplication.exception.ResourceNotFoundException;
import bg.softuni.invoiceapplication.mapper.user.UserMapper;
import bg.softuni.invoiceapplication.model.dto.users.UserProfileDTO;
import bg.softuni.invoiceapplication.model.dto.users.UserProfileEditRequestDTO;
import bg.softuni.invoiceapplication.model.dto.users.UserRegistrationRequestDTO;
import bg.softuni.invoiceapplication.model.dto.users.UserRegistrationResponseDTO;
import bg.softuni.invoiceapplication.model.dto.users.UserShowAllDTO;
import bg.softuni.invoiceapplication.model.entity.User;
import bg.softuni.invoiceapplication.model.enums.UserRole;
import bg.softuni.invoiceapplication.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserServiceImplTest {

    private static final UUID USER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID SECOND_USER_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final LocalDateTime CREATED_ON = LocalDateTime.of(2026, 8, 2, 12, 30);

    private FakeUserRepository fakeUserRepository;
    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        fakeUserRepository = new FakeUserRepository();

        userService = new UserServiceImpl(
                fakeUserRepository.repository(),
                new FakePasswordEncoder(),
                new UserMapper()
        );
    }

    @Test
    void registerUser_shouldSaveAdminUser_whenFirstUserRegisters() {
        UserRegistrationRequestDTO requestDTO = createRegistrationRequestDTO("lambi", "lambi@example.com");

        UserRegistrationResponseDTO result = userService.registerUser(requestDTO);

        assertThat(result.getUsername()).isEqualTo("lambi");
        assertThat(result.getEmail()).isEqualTo("lambi@example.com");
        assertThat(fakeUserRepository.savedUser.getPassword()).isEqualTo("encoded-password");
        assertThat(fakeUserRepository.savedUser.getRole()).isEqualTo(UserRole.ADMIN);
    }

    @Test
    void registerUser_shouldSaveRegularUser_whenUsersAlreadyExist() {
        fakeUserRepository.addUser(createUser(USER_ID, "admin", "admin@example.com", UserRole.ADMIN, true));
        UserRegistrationRequestDTO requestDTO = createRegistrationRequestDTO("newuser", "new@example.com");

        userService.registerUser(requestDTO);

        assertThat(fakeUserRepository.savedUser.getRole()).isEqualTo(UserRole.USER);
    }

    @Test
    void registerUser_shouldThrowException_whenPasswordsDoNotMatch() {
        UserRegistrationRequestDTO requestDTO = createRegistrationRequestDTO("lambi", "lambi@example.com");
        requestDTO.setPasswordConfirm("different");

        assertThatThrownBy(() -> userService.registerUser(requestDTO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Passwords do not match");

        assertThat(fakeUserRepository.savedUser).isNull();
    }

    @Test
    void registerUser_shouldThrowException_whenUsernameExists() {
        fakeUserRepository.addUser(createUser(USER_ID, "lambi", "old@example.com", UserRole.USER, true));
        UserRegistrationRequestDTO requestDTO = createRegistrationRequestDTO("lambi", "lambi@example.com");

        assertThatThrownBy(() -> userService.registerUser(requestDTO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Username is already in use");
    }

    @Test
    void registerUser_shouldThrowException_whenEmailExists() {
        fakeUserRepository.addUser(createUser(USER_ID, "other", "lambi@example.com", UserRole.USER, true));
        UserRegistrationRequestDTO requestDTO = createRegistrationRequestDTO("lambi", "lambi@example.com");

        assertThatThrownBy(() -> userService.registerUser(requestDTO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Email is already in use");
    }

    @Test
    void findAllUsers_shouldReturnUsersOrderedByUsername() {
        fakeUserRepository.addUser(createUser(USER_ID, "z-user", "z@example.com", UserRole.USER, true));
        fakeUserRepository.addUser(createUser(SECOND_USER_ID, "a-user", "a@example.com", UserRole.ADMIN, true));

        List<UserShowAllDTO> result = userService.findAllUsers();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getUsername()).isEqualTo("a-user");
        assertThat(result.get(1).getUsername()).isEqualTo("z-user");
    }

    @Test
    void findUsersByUsername_shouldReturnAllUsers_whenUsernameIsBlank() {
        fakeUserRepository.addUser(createUser(USER_ID, "lambi", "lambi@example.com", UserRole.USER, true));

        List<UserShowAllDTO> result = userService.findUsersByUsername("   ");

        assertThat(result).hasSize(1);
        assertThat(fakeUserRepository.findAllByUsernameCalled).isTrue();
    }

    @Test
    void findUsersByUsername_shouldSearchByTrimmedUsername_whenUsernameIsPresent() {
        fakeUserRepository.searchResult = List.of(createUser(USER_ID, "lambi", "lambi@example.com", UserRole.USER, true));

        List<UserShowAllDTO> result = userService.findUsersByUsername("  lam  ");

        assertThat(result).hasSize(1);
        assertThat(fakeUserRepository.lastSearchedUsername).isEqualTo("lam");
    }

    @Test
    void findUserProfile_shouldReturnProfileDTO_whenUserExists() {
        fakeUserRepository.addUser(createUser(USER_ID, "lambi", "lambi@example.com", UserRole.USER, true));

        UserProfileDTO result = userService.findUserProfile(USER_ID);

        assertThat(result.getId()).isEqualTo(USER_ID);
        assertThat(result.getUsername()).isEqualTo("lambi");
        assertThat(result.getEmail()).isEqualTo("lambi@example.com");
    }

    @Test
    void findUserProfile_shouldThrowException_whenUserDoesNotExist() {
        assertThatThrownBy(() -> userService.findUserProfile(USER_ID))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("User with id " + USER_ID + " does not exist");
    }

    @Test
    void getUserProfileForEdit_shouldReturnEditDTO_whenUserExists() {
        fakeUserRepository.addUser(createUser(USER_ID, "lambi", "lambi@example.com", UserRole.USER, true));

        UserProfileEditRequestDTO result = userService.getUserProfileForEdit(USER_ID);

        assertThat(result.getEmail()).isEqualTo("lambi@example.com");
        assertThat(result.getFirstName()).isEqualTo("Lyubomir");
        assertThat(result.getLastName()).isEqualTo("Lambov");
    }

    @Test
    void editUserProfile_shouldUpdateAndSaveUser_whenRequestIsValid() {
        User user = createUser(USER_ID, "lambi", "lambi@example.com", UserRole.USER, true);
        fakeUserRepository.addUser(user);
        UserProfileEditRequestDTO requestDTO = createProfileEditRequestDTO("updated@example.com");

        userService.editUserProfile(USER_ID, requestDTO);

        assertThat(user.getEmail()).isEqualTo("updated@example.com");
        assertThat(user.getFirstName()).isEqualTo("Updated");
        assertThat(user.getLastName()).isEqualTo("User");
        assertThat(fakeUserRepository.savedUser).isSameAs(user);
    }

    @Test
    void editUserProfile_shouldThrowException_whenRequestIsNull() {
        assertThatThrownBy(() -> userService.editUserProfile(USER_ID, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("User profile edit request must not be null");
    }

    @Test
    void editUserProfile_shouldThrowException_whenUserDoesNotExist() {
        UserProfileEditRequestDTO requestDTO = createProfileEditRequestDTO("updated@example.com");

        assertThatThrownBy(() -> userService.editUserProfile(USER_ID, requestDTO))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("User with id " + USER_ID + " does not exist");
    }

    @Test
    void editUserProfile_shouldThrowException_whenEmailBelongsToAnotherUser() {
        fakeUserRepository.addUser(createUser(USER_ID, "lambi", "lambi@example.com", UserRole.USER, true));
        fakeUserRepository.addUser(createUser(SECOND_USER_ID, "other", "updated@example.com", UserRole.USER, true));
        UserProfileEditRequestDTO requestDTO = createProfileEditRequestDTO("updated@example.com");

        assertThatThrownBy(() -> userService.editUserProfile(USER_ID, requestDTO))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Email is already in use");
    }

    @Test
    void toggleUserStatus_shouldChangeActiveFlagAndSaveUser_whenUserExists() {
        User user = createUser(USER_ID, "lambi", "lambi@example.com", UserRole.USER, true);
        fakeUserRepository.addUser(user);

        userService.toggleUserStatus(USER_ID);

        assertThat(user.isActive()).isFalse();
        assertThat(fakeUserRepository.savedUser).isSameAs(user);
    }

    @Test
    void toggleUserRole_shouldChangeUserToAdmin_whenUserRoleIsUser() {
        User user = createUser(USER_ID, "lambi", "lambi@example.com", UserRole.USER, true);
        fakeUserRepository.addUser(user);

        userService.toggleUserRole(USER_ID);

        assertThat(user.getRole()).isEqualTo(UserRole.ADMIN);
        assertThat(fakeUserRepository.savedUser).isSameAs(user);
    }

    @Test
    void toggleUserRole_shouldChangeAdminToUser_whenUserRoleIsAdmin() {
        User user = createUser(USER_ID, "lambi", "lambi@example.com", UserRole.ADMIN, true);
        fakeUserRepository.addUser(user);

        userService.toggleUserRole(USER_ID);

        assertThat(user.getRole()).isEqualTo(UserRole.USER);
        assertThat(fakeUserRepository.savedUser).isSameAs(user);
    }

    @Test
    void loadUserByUsername_shouldReturnAuthenticatedUserDetails_whenUserExists() {
        fakeUserRepository.addUser(createUser(USER_ID, "lambi", "lambi@example.com", UserRole.ADMIN, true));

        UserDetails result = userService.loadUserByUsername("lambi");

        assertThat(result.getUsername()).isEqualTo("lambi");
        assertThat(result.getPassword()).isEqualTo("encoded-password");
        assertThat(result.isEnabled()).isTrue();
        assertThat(result.getAuthorities())
                .extracting(Object::toString)
                .containsExactly("ROLE_ADMIN");
    }

    @Test
    void loadUserByUsername_shouldThrowException_whenUserDoesNotExist() {
        assertThatThrownBy(() -> userService.loadUserByUsername("missing"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage("missing");
    }

    private UserRegistrationRequestDTO createRegistrationRequestDTO(String username, String email) {
        return UserRegistrationRequestDTO.builder()
                .username(username)
                .password("password")
                .passwordConfirm("password")
                .email(email)
                .build();
    }

    private UserProfileEditRequestDTO createProfileEditRequestDTO(String email) {
        return UserProfileEditRequestDTO.builder()
                .email(email)
                .firstName("Updated")
                .lastName("User")
                .phoneNumber("+359899999999")
                .address("Plovdiv")
                .build();
    }

    private User createUser(UUID id, String username, String email, UserRole role, boolean active) {
        User user = User.builder()
                .username(username)
                .password("encoded-password")
                .email(email)
                .firstName("Lyubomir")
                .lastName("Lambov")
                .phoneNumber("+359888123456")
                .address("Sofia")
                .role(role)
                .active(active)
                .build();
        ReflectionTestUtils.setField(user, "id", id);
        ReflectionTestUtils.setField(user, "createdOn", CREATED_ON);
        return user;
    }

    private static final class FakePasswordEncoder implements PasswordEncoder {

        @Override
        public String encode(CharSequence rawPassword) {
            return "encoded-" + rawPassword;
        }

        @Override
        public boolean matches(CharSequence rawPassword, String encodedPassword) {
            return encodedPassword.equals(encode(rawPassword));
        }
    }

    private static final class FakeUserRepository {

        private final Map<UUID, User> users = new LinkedHashMap<>();

        private boolean findAllByUsernameCalled;
        private String lastSearchedUsername;
        private List<User> searchResult = List.of();
        private User savedUser;

        private void addUser(User user) {
            users.put(user.getId(), user);
        }

        private UserRepository repository() {
            return proxy(UserRepository.class, (proxy, method, args) -> switch (method.getName()) {
                case "findByUsername" -> users.values()
                        .stream()
                        .filter(user -> user.getUsername().equals(args[0]))
                        .findFirst();
                case "findByEmail" -> users.values()
                        .stream()
                        .filter(user -> user.getEmail().equals(args[0]))
                        .findFirst();
                case "count" -> (long) users.size();
                case "save" -> {
                    savedUser = (User) args[0];
                    yield savedUser;
                }
                case "findAllByOrderByUsernameAsc" -> {
                    findAllByUsernameCalled = true;
                    yield users.values()
                            .stream()
                            .sorted(Comparator.comparing(User::getUsername))
                            .toList();
                }
                case "findByUsernameContainingIgnoreCaseOrderByUsernameAsc" -> {
                    lastSearchedUsername = (String) args[0];
                    yield searchResult;
                }
                case "findById" -> Optional.ofNullable(users.get((UUID) args[0]));
                default -> defaultValue(method.getReturnType());
            });
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> T proxy(Class<T> type, InvocationHandler invocationHandler) {
        return (T) Proxy.newProxyInstance(
                type.getClassLoader(),
                new Class<?>[]{type},
                invocationHandler
        );
    }

    private static Object defaultValue(Class<?> returnType) {
        if (returnType.equals(boolean.class)) {
            return false;
        }

        if (returnType.equals(long.class)) {
            return 0L;
        }

        if (returnType.equals(Optional.class)) {
            return Optional.empty();
        }

        if (returnType.equals(List.class)) {
            return List.of();
        }

        return null;
    }
}
