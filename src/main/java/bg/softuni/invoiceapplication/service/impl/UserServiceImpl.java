package bg.softuni.invoiceapplication.service.impl;

import bg.softuni.invoiceapplication.mapper.user.UserMapper;
import bg.softuni.invoiceapplication.model.User;
import bg.softuni.invoiceapplication.model.dto.UserRegistrationRequestDTO;
import bg.softuni.invoiceapplication.model.dto.UserRegistrationResponseDTO;
import bg.softuni.invoiceapplication.repository.UserRepository;
import bg.softuni.invoiceapplication.service.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

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
            throw  new IllegalArgumentException("Passwords do not match");//! Да променя грешката, която хвърля
        }
        if (userRepository.findByUsername(userRegistrationRequestDTO.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Username is already in use"); //! Да променя грешката, която хвърля
        }
        if (userRepository.findByEmail(userRegistrationRequestDTO.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email is already in use");//! Да променя грешката, която хвърля
        }

        User userToRegister = userMapper.fromUserRegistrationRequestDTOtoUser(userRegistrationRequestDTO);

        String encodedPassword = passwordEncoder.encode(userRegistrationRequestDTO.getPassword());
        userToRegister.setPassword(encodedPassword);

        User savedUser = userRepository.save(userToRegister);

        UserRegistrationResponseDTO userRegistrationResponseDTO = userMapper.fromUserToUserRegistrationResponseDTO(savedUser);

        return userRegistrationResponseDTO;
    }
}