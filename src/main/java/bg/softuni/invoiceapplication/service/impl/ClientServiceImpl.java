package bg.softuni.invoiceapplication.service.impl;

import bg.softuni.invoiceapplication.mapper.client.ClientMapper;
import bg.softuni.invoiceapplication.model.dto.ClientCreateRequestDTO;
import bg.softuni.invoiceapplication.model.dto.ClientEditRequestDTO;
import bg.softuni.invoiceapplication.model.dto.ClientSelectDTO;
import bg.softuni.invoiceapplication.model.dto.ClientShowAllDTO;
import bg.softuni.invoiceapplication.model.entity.Client;
import bg.softuni.invoiceapplication.repository.ClientRepository;
import bg.softuni.invoiceapplication.repository.InvoiceRepository;
import bg.softuni.invoiceapplication.repository.PaymentRepository;
import bg.softuni.invoiceapplication.service.ClientService;
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
    private final PaymentRepository paymentRepository;
    private final ClientMapper clientMapper;

    public ClientServiceImpl(ClientRepository clientRepository,
                             InvoiceRepository invoiceRepository,
                             PaymentRepository paymentRepository,
                             ClientMapper clientMapper) {
        this.clientRepository = clientRepository;
        this.invoiceRepository = invoiceRepository;
        this.paymentRepository = paymentRepository;
        this.clientMapper = clientMapper;
    }

    @Override
    public Client createClient(ClientCreateRequestDTO clientCreateRequestDTO) {
        if (clientCreateRequestDTO == null) {
            throw new IllegalArgumentException("Client create request must not be null");
        }

        if (clientRepository.existsByDisplayName(clientCreateRequestDTO.getDisplayName())) {
            throw new RuntimeException("Client with display name " + clientCreateRequestDTO.getDisplayName() + " already exists");
        }

        normalizeVatNumber(clientCreateRequestDTO);

        if (clientCreateRequestDTO.getVatNumber() != null
                && clientRepository.existsByVatNumber(clientCreateRequestDTO.getVatNumber())) {
            throw new RuntimeException("Client with VAT number " + clientCreateRequestDTO.getVatNumber() + " already exists");
        }

        Client clientToSave = clientMapper.fromClientCreateRequestDTOToClient(clientCreateRequestDTO);
        return clientRepository.save(clientToSave);
    }

    @Override
    public List<ClientShowAllDTO> findAllClients() {
        List<Client> allClients = clientRepository.findAll(
                Sort.by(Sort.Order.desc("active"), Sort.Order.asc("displayName"))
        );

        return clientMapper.fromAllClientsToClientsShowAllDTO(allClients);
    }

    @Override
    public List<ClientSelectDTO> findAllActiveClientsForSelect() {
        return clientRepository.findAllByActiveTrueOrderByDisplayNameAsc()
                .stream()
                .map(client -> ClientSelectDTO.builder()
                        .id(client.getId())
                        .displayName(client.getDisplayName())
                        .build())
                .toList();
    }

    @Override
    public List<ClientSelectDTO> findAllActiveClientsForSelect(UUID selectedClientId) {
        List<ClientSelectDTO> activeClients = new ArrayList<>(findAllActiveClientsForSelect());

        boolean selectedClientAlreadyIncluded = activeClients.stream()
                .anyMatch(client -> client.getId().equals(selectedClientId));

        if (selectedClientId != null && !selectedClientAlreadyIncluded) {
            clientRepository.findById(selectedClientId)
                    .map(client -> ClientSelectDTO.builder()
                            .id(client.getId())
                            .displayName(client.getDisplayName())
                            .build())
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
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Client with id " + id + " does not exist"));

        return clientMapper.fromClientToClientEditRequestDTO(client);
    }

    @Override
    public Optional<String> findDuplicateFieldForEdit(ClientEditRequestDTO clientEditRequestDTO) {
        Optional<Client> clientByDisplayName = clientRepository.findByDisplayNameAndIdNot(
                clientEditRequestDTO.getDisplayName(),
                clientEditRequestDTO.getId());

        if (clientByDisplayName.isPresent()) {
            return Optional.of("displayName");
        }

        normalizeVatNumber(clientEditRequestDTO);

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

        if (clientRepository.existsByDisplayNameAndIdNot(
                clientEditRequestDTO.getDisplayName(),
                clientEditRequestDTO.getId())) {
            throw new RuntimeException("Client with display name " + clientEditRequestDTO.getDisplayName() + " already exists");
        }

        normalizeVatNumber(clientEditRequestDTO);

        if (clientEditRequestDTO.getVatNumber() != null
                && clientRepository.existsByVatNumberAndIdNot(
                clientEditRequestDTO.getVatNumber(),
                clientEditRequestDTO.getId())) {
            throw new RuntimeException("Client with VAT number " + clientEditRequestDTO.getVatNumber() + " already exists");
        }

        clientMapper.updateClientFromEditRequestDTO(clientToEdit, clientEditRequestDTO);
        clientRepository.save(clientToEdit);
    }

    @Override
    public void toggleClientActive(UUID id) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Client with id " + id + " does not exist"));

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

        if (paymentRepository.existsByClientId(id)) {
            throw new IllegalStateException("Client cannot be deleted because payments already exist");
        }

        clientRepository.delete(client);
    }

    private void normalizeVatNumber(ClientCreateRequestDTO clientCreateRequestDTO) {
        String vatNumber = clientCreateRequestDTO.getVatNumber();
        clientCreateRequestDTO.setVatNumber(normalizeVatNumber(vatNumber));
    }

    private void normalizeVatNumber(ClientEditRequestDTO clientEditRequestDTO) {
        String vatNumber = clientEditRequestDTO.getVatNumber();
        clientEditRequestDTO.setVatNumber(normalizeVatNumber(vatNumber));
    }

    private String normalizeVatNumber(String vatNumber) {
        if (vatNumber == null) {
            return null;
        }

        String normalizedVatNumber = vatNumber.trim();
        return normalizedVatNumber.isBlank() ? null : normalizedVatNumber.toUpperCase();
    }
}
