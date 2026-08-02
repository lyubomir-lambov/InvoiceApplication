package bg.softuni.invoiceapplication.mapper.user;

import bg.softuni.invoiceapplication.model.dto.users.UserProfileDTO;
import bg.softuni.invoiceapplication.model.dto.users.UserProfileEditRequestDTO;
import bg.softuni.invoiceapplication.model.dto.users.UserRegistrationRequestDTO;
import bg.softuni.invoiceapplication.model.dto.users.UserRegistrationResponseDTO;
import bg.softuni.invoiceapplication.model.dto.users.UserShowAllDTO;
import bg.softuni.invoiceapplication.model.entity.User;
import bg.softuni.invoiceapplication.model.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class UserMapperTest {

    private static final UUID USER_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");
    private static final LocalDateTime CREATED_ON = LocalDateTime.of(2026, 8, 2, 12, 30);

    private UserMapper userMapper;

    @BeforeEach
    void setUp() {
        userMapper = new UserMapper();
    }

    @Test
    void fromUserRegistrationRequestDTOtoUser_shouldMapUsernameAndEmail_whenRequestIsValid() {
        UserRegistrationRequestDTO requestDTO = createRegistrationRequestDTO();

        User result = userMapper.fromUserRegistrationRequestDTOtoUser(requestDTO);

        assertThat(result.getUsername()).isEqualTo("testuser");
        assertThat(result.getEmail()).isEqualTo("test@example.com");
        assertThat(result.getPassword()).isNull();
    }

    @Test
    void fromUserRegistrationRequestDTOtoUser_shouldReturnNull_whenRequestIsNull() {
        User result = userMapper.fromUserRegistrationRequestDTOtoUser(null);

        assertThat(result).isNull();
    }

    @Test
    void fromUserToUserRegistrationResponseDTO_shouldMapFields_whenUserIsValid() {
        User user = createUser();

        UserRegistrationResponseDTO result = userMapper.fromUserToUserRegistrationResponseDTO(user);

        assertThat(result.getUsername()).isEqualTo("testuser");
        assertThat(result.getEmail()).isEqualTo("test@example.com");
        assertThat(result.getCreatedOn()).isEqualTo(CREATED_ON);
    }

    @Test
    void fromUserToUserRegistrationResponseDTO_shouldReturnNull_whenUserIsNull() {
        UserRegistrationResponseDTO result = userMapper.fromUserToUserRegistrationResponseDTO(null);

        assertThat(result).isNull();
    }

    @Test
    void fromAllUsersToUserShowAllDTOs_shouldMapUsers_whenUsersExist() {
        User user = createUser();

        List<UserShowAllDTO> result = userMapper.fromAllUsersToUserShowAllDTOs(List.of(user));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(USER_ID);
        assertThat(result.get(0).getUsername()).isEqualTo("testuser");
        assertThat(result.get(0).getEmail()).isEqualTo("test@example.com");
        assertThat(result.get(0).getRole()).isEqualTo(UserRole.ADMIN);
        assertThat(result.get(0).isActive()).isTrue();
        assertThat(result.get(0).getCreatedOn()).isEqualTo(CREATED_ON);
    }

    @Test
    void fromAllUsersToUserShowAllDTOs_shouldReturnNull_whenUsersAreNull() {
        List<UserShowAllDTO> result = userMapper.fromAllUsersToUserShowAllDTOs(null);

        assertThat(result).isNull();
    }

    @Test
    void fromUserToUserProfileDTO_shouldMapProfileFields_whenUserIsValid() {
        User user = createUser();

        UserProfileDTO result = userMapper.fromUserToUserProfileDTO(user);

        assertThat(result.getId()).isEqualTo(USER_ID);
        assertThat(result.getUsername()).isEqualTo("testuser");
        assertThat(result.getEmail()).isEqualTo("test@example.com");
        assertThat(result.getFirstName()).isEqualTo("Test");
        assertThat(result.getLastName()).isEqualTo("User");
        assertThat(result.getPhoneNumber()).isEqualTo("+359888123456");
        assertThat(result.getAddress()).isEqualTo("Sofia, Bulgaria");
        assertThat(result.getRole()).isEqualTo(UserRole.ADMIN);
    }

    @Test
    void fromUserToUserProfileDTO_shouldReturnNull_whenUserIsNull() {
        UserProfileDTO result = userMapper.fromUserToUserProfileDTO(null);

        assertThat(result).isNull();
    }

    @Test
    void fromUserToUserProfileEditRequestDTO_shouldMapEditableFields_whenUserIsValid() {
        User user = createUser();

        UserProfileEditRequestDTO result = userMapper.fromUserToUserProfileEditRequestDTO(user);

        assertThat(result.getEmail()).isEqualTo("test@example.com");
        assertThat(result.getFirstName()).isEqualTo("Test");
        assertThat(result.getLastName()).isEqualTo("User");
        assertThat(result.getPhoneNumber()).isEqualTo("+359888123456");
        assertThat(result.getAddress()).isEqualTo("Sofia, Bulgaria");
    }

    @Test
    void fromUserToUserProfileEditRequestDTO_shouldReturnNull_whenUserIsNull() {
        UserProfileEditRequestDTO result = userMapper.fromUserToUserProfileEditRequestDTO(null);

        assertThat(result).isNull();
    }

    @Test
    void updateUserFromProfileEditRequestDTO_shouldUpdateEditableFields_whenRequestIsValid() {
        User user = createUser();
        UserProfileEditRequestDTO requestDTO = UserProfileEditRequestDTO.builder()
                .email("updated@example.com")
                .firstName("Updated")
                .lastName("Name")
                .phoneNumber("+359899999999")
                .address("Plovdiv, Bulgaria")
                .build();

        userMapper.updateUserFromProfileEditRequestDTO(user, requestDTO);

        assertThat(user.getEmail()).isEqualTo("updated@example.com");
        assertThat(user.getFirstName()).isEqualTo("Updated");
        assertThat(user.getLastName()).isEqualTo("Name");
        assertThat(user.getPhoneNumber()).isEqualTo("+359899999999");
        assertThat(user.getAddress()).isEqualTo("Plovdiv, Bulgaria");
        assertThat(user.getUsername()).isEqualTo("testuser");
        assertThat(user.getRole()).isEqualTo(UserRole.ADMIN);
    }

    @Test
    void updateUserFromProfileEditRequestDTO_shouldDoNothing_whenUserIsNull() {
        UserProfileEditRequestDTO requestDTO = UserProfileEditRequestDTO.builder()
                .email("updated@example.com")
                .build();

        userMapper.updateUserFromProfileEditRequestDTO(null, requestDTO);
    }

    @Test
    void updateUserFromProfileEditRequestDTO_shouldDoNothing_whenRequestIsNull() {
        User user = createUser();

        userMapper.updateUserFromProfileEditRequestDTO(user, null);

        assertThat(user.getEmail()).isEqualTo("test@example.com");
        assertThat(user.getFirstName()).isEqualTo("Test");
    }

    private UserRegistrationRequestDTO createRegistrationRequestDTO() {
        return UserRegistrationRequestDTO.builder()
                .username("testuser")
                .password("password")
                .passwordConfirm("password")
                .email("test@example.com")
                .build();
    }

    private User createUser() {
        User user = User.builder()
                .username("testuser")
                .password("encoded-password")
                .email("test@example.com")
                .firstName("Test")
                .lastName("User")
                .phoneNumber("+359888123456")
                .address("Sofia, Bulgaria")
                .active(true)
                .role(UserRole.ADMIN)
                .build();
        ReflectionTestUtils.setField(user, "id", USER_ID);
        ReflectionTestUtils.setField(user, "createdOn", CREATED_ON);
        return user;
    }
}
