package bg.softuni.invoiceapplication.service.impl;

import bg.softuni.invoiceapplication.mapper.client.ClientMapper;
import bg.softuni.invoiceapplication.model.Client;
import bg.softuni.invoiceapplication.model.dto.ClientCreateRequestDTO;
import bg.softuni.invoiceapplication.model.dto.ClientShowAllDTO;
import bg.softuni.invoiceapplication.repository.ClientRepository;
import bg.softuni.invoiceapplication.service.ClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ClientServiceImpl implements ClientService {
    private final ClientRepository clientRepository;
    private final ClientMapper clientMapper;

    @Autowired
    public ClientServiceImpl(ClientRepository clientRepository, ClientMapper clientMapper) {
        this.clientRepository = clientRepository;
        this.clientMapper = clientMapper;
    }

    @Override
    public Client createClient(ClientCreateRequestDTO clientCreateRequestDTO) {
        if (clientRepository.existsByDisplayName(clientCreateRequestDTO.getDisplayName())) {
            throw new RuntimeException("Client with display name " + clientCreateRequestDTO.getDisplayName() + " already exists");
            //! След време да сменя грешката
        }
        String vatNumber = clientCreateRequestDTO.getVatNumber();
        if (vatNumber != null && !vatNumber.isBlank()
                && clientRepository.existsByVatNumber(vatNumber)) {
            throw new RuntimeException("Client with vatNumber " + vatNumber + " already exists");
            //! След време да сменя грешката
        }

        Client clientToSave = clientMapper.fromClientCreateRequestDTOToClient(clientCreateRequestDTO);
        Client savedClient = clientRepository.save(clientToSave);
        return savedClient;

    }

    @Override
    public List<ClientShowAllDTO> findAllClients() {

        List<Client> allClients = clientRepository.findAll();
        List<ClientShowAllDTO> clientShowAllDTOS = clientMapper.fromAllClientsToClientsShowAllDTO(allClients);

        return clientShowAllDTOS;
    }

}
