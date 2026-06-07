package bg.softuni.invoiceapplication.service;

import bg.softuni.invoiceapplication.model.dto.UserLoginRequestDTO;
import bg.softuni.invoiceapplication.model.dto.UserLoginResponseDTO;
import bg.softuni.invoiceapplication.model.dto.UserRegistrationRequestDTO;
import bg.softuni.invoiceapplication.model.dto.UserRegistrationResponseDTO;

public interface UserService {
    UserRegistrationResponseDTO registerUser(UserRegistrationRequestDTO userRegistrationRequestDTO);
    UserLoginResponseDTO login(UserLoginRequestDTO userLoginRequestDTO);
}
