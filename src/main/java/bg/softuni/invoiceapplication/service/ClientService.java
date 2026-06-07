package bg.softuni.invoiceapplication.service;

import bg.softuni.invoiceapplication.model.Client;
import bg.softuni.invoiceapplication.model.dto.ClientCreateRequestDTO;
import bg.softuni.invoiceapplication.model.dto.ClientShowAllDTO;

import java.util.List;

public interface ClientService {
    Client createClient(ClientCreateRequestDTO clientCreateRequestDTO);
    List<ClientShowAllDTO> findAllClients();
}
