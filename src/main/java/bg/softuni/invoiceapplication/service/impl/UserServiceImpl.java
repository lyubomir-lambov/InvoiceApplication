package bg.softuni.invoiceapplication.service.impl;

import bg.softuni.invoiceapplication.repository.UserRepository;
import bg.softuni.invoiceapplication.service.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }
}




//        if (userRegistrationRequestDTO.getEmail().equals(userRegistrationRequestDTO.getPasswordConfirm())){
//        return null; //! Какво ще върна ако не съвпадат двете пароли
//        }

//! Проверка дали съвпадат двета пароли