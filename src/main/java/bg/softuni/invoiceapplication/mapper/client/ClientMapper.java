package bg.softuni.invoiceapplication.mapper.client;

import bg.softuni.invoiceapplication.model.dto.ClientCreateRequestDTO;
import bg.softuni.invoiceapplication.model.dto.ClientEditRequestDTO;
import bg.softuni.invoiceapplication.model.dto.ClientShowAllDTO;
import bg.softuni.invoiceapplication.model.entity.Client;
import org.springframework.stereotype.Component;

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
                .legalRepresentative(clientCreateRequestDTO.getLegalRepresentative())
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

        return clients.stream()
                .map(client -> ClientShowAllDTO.builder()
                        .id(client.getId())
                        .displayName(client.getDisplayName())
                        .companyName(client.getCompanyName())
                        .legalRepresentative(client.getLegalRepresentative())
                        .email(client.getEmail())
                        .phoneNumber(client.getPhoneNumber())
                        .country(client.getCountry())
                        .address(client.getAddress())
                        .vatRegistered(client.isVatRegistered())
                        .vatNumber(client.getVatNumber())
                        .active(client.isActive())
                        .build())
                .toList();
    }

    public ClientEditRequestDTO fromClientToClientEditRequestDTO(Client client) {
        if (client == null) {
            return null;
        }

        return ClientEditRequestDTO.builder()
                .id(client.getId())
                .displayName(client.getDisplayName())
                .companyName(client.getCompanyName())
                .legalRepresentative(client.getLegalRepresentative())
                .email(client.getEmail())
                .phoneNumber(client.getPhoneNumber())
                .country(client.getCountry())
                .address(client.getAddress())
                .vatRegistered(client.isVatRegistered())
                .vatNumber(client.getVatNumber())
                .build();
    }

    public void updateClientFromEditRequestDTO(Client client, ClientEditRequestDTO clientEditRequestDTO) {
        client.setDisplayName(clientEditRequestDTO.getDisplayName());
        client.setCompanyName(clientEditRequestDTO.getCompanyName());
        client.setLegalRepresentative(clientEditRequestDTO.getLegalRepresentative());
        client.setEmail(clientEditRequestDTO.getEmail());
        client.setPhoneNumber(clientEditRequestDTO.getPhoneNumber());
        client.setCountry(clientEditRequestDTO.getCountry());
        client.setAddress(clientEditRequestDTO.getAddress());
        client.setVatRegistered(clientEditRequestDTO.getVatRegistered());
        client.setVatNumber(clientEditRequestDTO.getVatNumber());
    }
}
