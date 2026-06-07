package bg.softuni.invoiceapplication.service.impl;

import bg.softuni.invoiceapplication.mapper.client.ClientMapper;
import bg.softuni.invoiceapplication.model.Client;
import bg.softuni.invoiceapplication.model.dto.ClientCreateRequestDTO;
import bg.softuni.invoiceapplication.model.dto.ClientEditRequestDTO;
import bg.softuni.invoiceapplication.model.dto.ClientShowAllDTO;
import bg.softuni.invoiceapplication.repository.ClientRepository;
import bg.softuni.invoiceapplication.service.ClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

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
        if (vatNumber != null) {
            vatNumber = vatNumber.trim();
            clientCreateRequestDTO.setVatNumber(vatNumber.isBlank() ? null : vatNumber.toUpperCase());
        }

        if (clientCreateRequestDTO.getVatNumber() != null
                && clientRepository.existsByVatNumber(clientCreateRequestDTO.getVatNumber())) {
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

    @Override
    public ClientEditRequestDTO getClientForEdit(UUID id) {

        Client clientById = clientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Client with id " + id + " does not exist"));
        //! Да се оправи грешката след време
        ClientEditRequestDTO clientEditRequestDTO = clientMapper.fromClientToClientEditRequestDTO(clientById);

        return clientEditRequestDTO;
    }

    @Override
    public void editClient(ClientEditRequestDTO clientEditRequestDTO) {
        Client clientToEdit = clientRepository.findById(clientEditRequestDTO.getId())
                .orElseThrow(() -> new RuntimeException("Client with id " + clientEditRequestDTO.getId() + " does not exist"));
        //! Да коригирам грешката след време

        String vatNumber = clientEditRequestDTO.getVatNumber();
        if (vatNumber != null) {
            vatNumber = vatNumber.trim();
            clientEditRequestDTO.setVatNumber(vatNumber.isBlank() ? null : vatNumber.toUpperCase());
        }

        if (clientEditRequestDTO.getVatNumber() != null
                && clientRepository.existsByVatNumberAndIdNot(
                clientEditRequestDTO.getVatNumber(),
                clientEditRequestDTO.getId())) {
            throw new RuntimeException("Client with vatNumber " + clientEditRequestDTO.getVatNumber() + " already exists");
            //! Да коригирам грешката след време
        }

        clientMapper.updateClientFromEditRequestDTO(clientToEdit, clientEditRequestDTO);

        clientRepository.save(clientToEdit);
    }


}
