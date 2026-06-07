package bg.softuni.invoiceapplication.mapper.client;

import bg.softuni.invoiceapplication.model.Client;
import bg.softuni.invoiceapplication.model.dto.ClientCreateRequestDTO;
import bg.softuni.invoiceapplication.model.dto.ClientShowAllDTO;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

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

    public List<ClientShowAllDTO> fromAllClientsToClientsShowAllDTO(List<Client> clients) {
        if (clients == null) {
            return null;
        }

        List<ClientShowAllDTO> clientShowAllDTOs = new ArrayList<>();
        clients.forEach(client -> {
            ClientShowAllDTO clientShowAllDTO = ClientShowAllDTO.builder()
                    .id(client.getId())
                    .displayName(client.getDisplayName())
                    .companyName(client.getCompanyName())
                    .email(client.getEmail())
                    .phoneNumber(client.getPhoneNumber())
                    .country(client.getCountry())
                    .address(client.getAddress())
                    .vatRegistered(client.isVatRegistered())
                    .vatNumber(client.getVatNumber())
                    .active(client.isActive())
                    .build();
            clientShowAllDTOs.add(clientShowAllDTO);
        });
        return clientShowAllDTOs;
    }
}
