package bg.softuni.invoiceapplication.service.impl;

import bg.softuni.invoiceapplication.mapper.client.ClientMapper;
import bg.softuni.invoiceapplication.model.entity.Client;
import bg.softuni.invoiceapplication.model.dto.ClientCreateRequestDTO;
import bg.softuni.invoiceapplication.model.dto.ClientEditRequestDTO;
import bg.softuni.invoiceapplication.model.dto.ClientSelectDTO;
import bg.softuni.invoiceapplication.model.dto.ClientShowAllDTO;
import bg.softuni.invoiceapplication.repository.ClientRepository;
import bg.softuni.invoiceapplication.repository.InvoiceRepository;
import bg.softuni.invoiceapplication.service.ClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ClientServiceImpl implements ClientService {
    private final ClientRepository clientRepository;
    private final InvoiceRepository invoiceRepository;
    private final ClientMapper clientMapper;

    @Autowired
    public ClientServiceImpl(ClientRepository clientRepository, InvoiceRepository invoiceRepository, ClientMapper clientMapper) {
        this.clientRepository = clientRepository;
        this.invoiceRepository = invoiceRepository;
        this.clientMapper = clientMapper;
    }

    @Override
    public Client createClient(ClientCreateRequestDTO clientCreateRequestDTO) {
        if (clientCreateRequestDTO == null) {
            throw new IllegalArgumentException("Client create request must not be null");
        }

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

        List<Client> allClients = clientRepository.findAll(
                Sort.by(Sort.Order.desc("active"), Sort.Order.asc("displayName"))
        );
        List<ClientShowAllDTO> clientShowAllDTOS = clientMapper.fromAllClientsToClientsShowAllDTO(allClients);

        return clientShowAllDTOS;
    }

    @Override
    public List<ClientSelectDTO> findAllActiveClientsForSelect() {
        return clientRepository.findAllByActiveTrueOrderByDisplayNameAsc()
                .stream()
                .map(client -> new ClientSelectDTO(client.getId(), client.getDisplayName()))
                .toList();
    }

    @Override
    public List<ClientSelectDTO> findAllActiveClientsForSelect(UUID selectedClientId) {
        List<ClientSelectDTO> activeClients = new ArrayList<>(findAllActiveClientsForSelect());

        boolean selectedClientAlreadyIncluded = activeClients.stream()
                .anyMatch(client -> client.getId().equals(selectedClientId));

        if (selectedClientId != null && !selectedClientAlreadyIncluded) {
            clientRepository.findById(selectedClientId)
                    .map(client -> new ClientSelectDTO(client.getId(), client.getDisplayName()))
                    .ifPresent(client -> activeClients.add(0, client));
        }

        return activeClients;
    }

    @Override
    public List<ClientShowAllDTO> findClientsByName(String clientName) {
        if (clientName == null || clientName.isBlank()) {
            return findAllClients();
        }

        String searchedClientName = clientName.trim();
        List<Client> clients = clientRepository.findByDisplayNameContainingIgnoreCaseOrCompanyNameContainingIgnoreCase(
                searchedClientName,
                searchedClientName
        );

        return clientMapper.fromAllClientsToClientsShowAllDTO(clients);
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
    public Optional<String> findDuplicateFieldForEdit(ClientEditRequestDTO clientEditRequestDTO) {
        Optional<Client> clientByDisplayName = clientRepository.findByDisplayNameAndIdNot(
                clientEditRequestDTO.getDisplayName(),
                clientEditRequestDTO.getId());

        if (clientByDisplayName.isPresent()) {
            return Optional.of("displayName");
        }

        String vatNumber = clientEditRequestDTO.getVatNumber();
        if (vatNumber != null) {
            vatNumber = vatNumber.trim();
            clientEditRequestDTO.setVatNumber(vatNumber.isBlank() ? null : vatNumber.toUpperCase());
        }

        if (clientEditRequestDTO.getVatNumber() == null) {
            return Optional.empty();
        }

        return clientRepository.findByVatNumberAndIdNot(
                clientEditRequestDTO.getVatNumber(),
                clientEditRequestDTO.getId()
        ).map(client -> "vatNumber");
    }

    @Override
    public void editClient(ClientEditRequestDTO clientEditRequestDTO) {
        Client clientToEdit = clientRepository.findById(clientEditRequestDTO.getId())
                .orElseThrow(() -> new RuntimeException("Client with id " + clientEditRequestDTO.getId() + " does not exist"));
        //! Да коригирам грешката след време

        if (clientRepository.existsByDisplayNameAndIdNot(
                clientEditRequestDTO.getDisplayName(),
                clientEditRequestDTO.getId())) {
            throw new RuntimeException("Client with display name " + clientEditRequestDTO.getDisplayName() + " already exists");
            //! Да коригирам грешката след време
        }

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

    @Override
    public void toggleClientActive(UUID id) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Client with id " + id + " does not exist"));
        //! Да коригирам грешката след време
        client.setActive(!client.isActive());
        clientRepository.save(client);
    }

    @Override
    public void deleteClient(UUID id) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Client with id " + id + " does not exist"));

        if (invoiceRepository.existsByClientId(id)) {
            throw new IllegalStateException("Client cannot be deleted because issued invoices already exist");
        }

        clientRepository.delete(client);
    }

}
