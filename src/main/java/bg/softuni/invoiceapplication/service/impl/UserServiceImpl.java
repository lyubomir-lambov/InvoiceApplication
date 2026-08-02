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
import bg.softuni.invoiceapplication.security.AuthenticatedUserDetails;
import bg.softuni.invoiceapplication.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService, UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

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
    public UserProfileDTO findUserProfile(UUID userId) {
        return userRepository.findById(userId)
                .map(userMapper::fromUserToUserProfileDTO)
                .orElseThrow(() -> new ResourceNotFoundException("User with id " + userId + " does not exist"));
    }

    @Override
    public UserProfileEditRequestDTO getUserProfileForEdit(UUID userId) {
        return userRepository.findById(userId)
                .map(userMapper::fromUserToUserProfileEditRequestDTO)
                .orElseThrow(() -> new ResourceNotFoundException("User with id " + userId + " does not exist"));
    }

    @Override
    public void editUserProfile(UUID userId, UserProfileEditRequestDTO userProfileEditRequestDTO) {
        if (userProfileEditRequestDTO == null) {
            throw new IllegalArgumentException("User profile edit request must not be null");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User with id " + userId + " does not exist"));

        userRepository.findByEmail(userProfileEditRequestDTO.getEmail())
                .filter(existingUser -> !existingUser.getId().equals(userId))
                .ifPresent(existingUser -> {
                    throw new BusinessRuleException("Email is already in use");
                });

        userMapper.updateUserFromProfileEditRequestDTO(user, userProfileEditRequestDTO);
        userRepository.save(user);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public void toggleUserStatus(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User with id " + userId + " does not exist"));

        user.setActive(!user.isActive());
        userRepository.save(user);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public void toggleUserRole(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User with id " + userId + " does not exist"));

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
