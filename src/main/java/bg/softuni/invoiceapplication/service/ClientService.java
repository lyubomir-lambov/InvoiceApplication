package bg.softuni.invoiceapplication.service;

import bg.softuni.invoiceapplication.model.entity.Client;
import bg.softuni.invoiceapplication.model.dto.ClientCreateRequestDTO;
import bg.softuni.invoiceapplication.model.dto.ClientEditRequestDTO;
import bg.softuni.invoiceapplication.model.dto.ClientSelectDTO;
import bg.softuni.invoiceapplication.model.dto.ClientShowAllDTO;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ClientService {
    Client createClient(ClientCreateRequestDTO clientCreateRequestDTO);
    List<ClientShowAllDTO> findAllClients();
    List<ClientSelectDTO> findAllActiveClientsForSelect();
    List<ClientSelectDTO> findAllActiveClientsForSelect(UUID selectedClientId);
    List<ClientShowAllDTO> findClientsByName(String clientName);
    ClientEditRequestDTO getClientForEdit(UUID id);
    Optional<String> findDuplicateFieldForEdit(ClientEditRequestDTO clientEditRequestDTO);
    void editClient(ClientEditRequestDTO clientEditRequestDTO);
    void toggleClientActive(UUID id);
    void deleteClient(UUID id);

}
