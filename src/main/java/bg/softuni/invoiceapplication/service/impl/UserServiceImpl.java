package bg.softuni.invoiceapplication.service.impl;

import bg.softuni.invoiceapplication.repository.UserRepository;
import bg.softuni.invoiceapplication.service.UserService;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
}
