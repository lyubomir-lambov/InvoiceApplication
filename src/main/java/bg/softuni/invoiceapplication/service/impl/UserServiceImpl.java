package bg.softuni.invoiceapplication.service.impl;

import bg.softuni.invoiceapplication.mapper.user.UserMapper;
import bg.softuni.invoiceapplication.model.dto.UserRegistrationRequestDTO;
import bg.softuni.invoiceapplication.model.dto.UserRegistrationResponseDTO;
import bg.softuni.invoiceapplication.model.dto.UserShowAllDTO;
import bg.softuni.invoiceapplication.model.entity.User;
import bg.softuni.invoiceapplication.model.enums.UserRole;
import bg.softuni.invoiceapplication.repository.UserRepository;
import bg.softuni.invoiceapplication.security.AuthenticatedUserDetails;
import bg.softuni.invoiceapplication.service.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService, UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userMapper = userMapper;
    }

    @Override
    public UserRegistrationResponseDTO registerUser(UserRegistrationRequestDTO userRegistrationRequestDTO) {
        if (!userRegistrationRequestDTO.getPassword().equals(userRegistrationRequestDTO.getPasswordConfirm())) {
            throw new IllegalArgumentException("Passwords do not match");
        }

        if (userRepository.findByUsername(userRegistrationRequestDTO.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Username is already in use");
        }

        if (userRepository.findByEmail(userRegistrationRequestDTO.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email is already in use");
        }

        User userToRegister = userMapper.fromUserRegistrationRequestDTOtoUser(userRegistrationRequestDTO);
        userToRegister.setPassword(passwordEncoder.encode(userRegistrationRequestDTO.getPassword()));
        if (userRepository.count() == 0) {
            userToRegister.setRole(UserRole.ADMIN);
        }

        User savedUser = userRepository.save(userToRegister);
        return userMapper.fromUserToUserRegistrationResponseDTO(savedUser);
    }

    @Override
    public List<UserShowAllDTO> findAllUsers() {
        return userMapper.fromAllUsersToUserShowAllDTOs(userRepository.findAllByOrderByUsernameAsc());
    }

    @Override
    public List<UserShowAllDTO> findUsersByUsername(String username) {
        if (username == null || username.isBlank()) {
            return findAllUsers();
        }

        return userMapper.fromAllUsersToUserShowAllDTOs(
                userRepository.findByUsernameContainingIgnoreCaseOrderByUsernameAsc(username.trim())
        );
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public void toggleUserStatus(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User with id " + userId + " does not exist"));

        user.setActive(!user.isActive());
        userRepository.save(user);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public void toggleUserRole(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User with id " + userId + " does not exist"));

        user.setRole(UserRole.ADMIN.equals(user.getRole()) ? UserRole.USER : UserRole.ADMIN);
        userRepository.save(user);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(username));

        return new AuthenticatedUserDetails(
                user.getId(),
                user.getUsername(),
                user.getPassword(),
                user.getRole(),
                user.isActive()
        );
    }
}
