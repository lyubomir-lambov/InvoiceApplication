package bg.softuni.invoiceapplication.service;

import bg.softuni.invoiceapplication.model.Client;
import bg.softuni.invoiceapplication.model.dto.ClientCreateRequestDTO;

public interface ClientService {
    Client createClient(ClientCreateRequestDTO clientCreateRequestDTO);
}
