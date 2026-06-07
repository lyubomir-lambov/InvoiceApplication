package bg.softuni.invoiceapplication.mapper.client;

import bg.softuni.invoiceapplication.model.Client;
import bg.softuni.invoiceapplication.model.dto.ClientCreateRequestDTO;
import org.springframework.stereotype.Component;

@Component
public class ClientMapper {
    public Client fromClientCreateRequestDTOToClient(ClientCreateRequestDTO clientCreateRequestDTO) {
        if (clientCreateRequestDTO == null) {
            return null;
        }

        return Client.builder()
                .displayName(clientCreateRequestDTO.getDisplayName())
                .companyName(clientCreateRequestDTO.getCompanyName())
                .email(clientCreateRequestDTO.getEmail())
                .phoneNumber(clientCreateRequestDTO.getPhoneNumber())
                .country(clientCreateRequestDTO.getCountry())
                .address(clientCreateRequestDTO.getAddress())
                .vatRegistered(clientCreateRequestDTO.getVatRegistered())
                .vatNumber(clientCreateRequestDTO.getVatNumber())
                .build();
    }
}
