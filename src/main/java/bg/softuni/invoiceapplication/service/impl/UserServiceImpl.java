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

        //! Проверка дали съвпадат двете пароли
        //! Проверка дали съшествува user  със същото име
        //! Проверка дали съшествува user  със същия email
        //! encode password
        String encodedPassword = passwordEncoder.encode(userRegistrationRequestDTO.getPassword());
        //! new pass to DTO
        userRegistrationRequestDTO.setPassword(encodedPassword);
        //! mapper to user
        User userToRegister = UserMapper.fromUserRegistrationRequestDTOtoUser(userRegistrationRequestDTO);
        //! user Save
        userRepository.save(userToRegister);
        //! user toDTO
        UserRegistrationResponseDTO userRegistrationResponseDTO = UserMapper.......
        //! return DTO



        return null;
    }
}




//        if (userRegistrationRequestDTO.getEmail().equals(userRegistrationRequestDTO.getPasswordConfirm())){
//        return null; //! Какво ще върна ако не съвпадат двете пароли
//        }

//! Проверка дали съвпадат двета пароли