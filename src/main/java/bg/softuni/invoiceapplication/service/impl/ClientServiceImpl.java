package bg.softuni.invoiceapplication.service.impl;

import bg.softuni.invoiceapplication.exception.BusinessRuleException;
import bg.softuni.invoiceapplication.exception.ResourceNotFoundException;
import bg.softuni.invoiceapplication.mapper.client.ClientMapper;
import bg.softuni.invoiceapplication.model.dto.clients.ClientCreateRequestDTO;
import bg.softuni.invoiceapplication.model.dto.clients.ClientEditRequestDTO;
import bg.softuni.invoiceapplication.model.dto.clients.ClientSelectDTO;
import bg.softuni.invoiceapplication.model.dto.clients.ClientShowAllDTO;
import bg.softuni.invoiceapplication.model.entity.Client;
import bg.softuni.invoiceapplication.repository.ClientRepository;
import bg.softuni.invoiceapplication.repository.InvoiceRepository;
import bg.softuni.invoiceapplication.repository.PaymentRepository;
import bg.softuni.invoiceapplication.service.ClientService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class ClientServiceImpl implements ClientService {

    private static final String ACTIVE_CLIENTS_FOR_SELECT_CACHE = "activeClientsForSelect";

    private final ClientRepository clientRepository;
    private final InvoiceRepository invoiceRepository;
    private final PaymentRepository paymentRepository;
    private final ClientMapper clientMapper;

    @Override
    @CacheEvict(value = ACTIVE_CLIENTS_FOR_SELECT_CACHE, allEntries = true)
    public Client createClient(ClientCreateRequestDTO clientCreateRequestDTO) {
        if (clientCreateRequestDTO == null) {
            throw new IllegalArgumentException("Client create request must not be null");
        }

        if (clientRepository.existsByDisplayName(clientCreateRequestDTO.getDisplayName())) {
            throw new BusinessRuleException("Client with display name " + clientCreateRequestDTO.getDisplayName() + " already exists");
        }

        normalizeVatNumber(clientCreateRequestDTO);

        if (clientCreateRequestDTO.getVatNumber() != null
                && clientRepository.existsByVatNumber(clientCreateRequestDTO.getVatNumber())) {
            throw new BusinessRuleException("Client with VAT number " + clientCreateRequestDTO.getVatNumber() + " already exists");
        }

        Client clientToSave = clientMapper.fromClientCreateRequestDTOToClient(clientCreateRequestDTO);
        Client savedClient = clientRepository.save(clientToSave);
        log.info("Client created: clientId={}, displayName={}, companyName={}",
                savedClient.getId(),
                savedClient.getDisplayName(),
                savedClient.getCompanyName());
        return savedClient;
    }

    @Override
    public List<ClientShowAllDTO> findAllClients() {
        List<Client> allClients = clientRepository.findAll(
                Sort.by(Sort.Order.desc("active"), Sort.Order.asc("displayName"))
        );

        return clientMapper.fromAllClientsToClientsShowAllDTO(allClients);
    }

    @Override
    @Cacheable(value = ACTIVE_CLIENTS_FOR_SELECT_CACHE, key = "'all'")
    public List<ClientSelectDTO> findAllActiveClientsForSelect() {
        return clientRepository.findAllByActiveTrueOrderByDisplayNameAsc()
                .stream()
                .map(this::mapToClientSelectDTO)
                .toList();
    }

    @Override
    @Cacheable(value = ACTIVE_CLIENTS_FOR_SELECT_CACHE, key = "#selectedClientId")
    public List<ClientSelectDTO> findAllActiveClientsForSelect(UUID selectedClientId) {
        List<ClientSelectDTO> activeClients = clientRepository.findAllByActiveTrueOrderByDisplayNameAsc()
                .stream()
                .map(this::mapToClientSelectDTO)
                .collect(java.util.stream.Collectors.toCollection(ArrayList::new));

        boolean selectedClientAlreadyIncluded = activeClients.stream()
                .anyMatch(client -> client.getId().equals(selectedClientId));

        if (selectedClientId != null && !selectedClientAlreadyIncluded) {
            clientRepository.findById(selectedClientId)
                    .map(this::mapToClientSelectDTO)
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
                .orElseThrow(() -> new ResourceNotFoundException("Client with id " + id + " does not exist"));

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
    @CacheEvict(value = ACTIVE_CLIENTS_FOR_SELECT_CACHE, allEntries = true)
    public void editClient(ClientEditRequestDTO clientEditRequestDTO) {
        Client clientToEdit = clientRepository.findById(clientEditRequestDTO.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Client with id " + clientEditRequestDTO.getId() + " does not exist"));

        if (clientRepository.existsByDisplayNameAndIdNot(
                clientEditRequestDTO.getDisplayName(),
                clientEditRequestDTO.getId())) {
            throw new BusinessRuleException("Client with display name " + clientEditRequestDTO.getDisplayName() + " already exists");
        }

        normalizeVatNumber(clientEditRequestDTO);

        if (clientEditRequestDTO.getVatNumber() != null
                && clientRepository.existsByVatNumberAndIdNot(
                clientEditRequestDTO.getVatNumber(),
                clientEditRequestDTO.getId())) {
            throw new BusinessRuleException("Client with VAT number " + clientEditRequestDTO.getVatNumber() + " already exists");
        }

        clientMapper.updateClientFromEditRequestDTO(clientToEdit, clientEditRequestDTO);
        clientRepository.save(clientToEdit);
        log.info("Client edited: clientId={}, displayName={}, companyName={}",
                clientToEdit.getId(),
                clientToEdit.getDisplayName(),
                clientToEdit.getCompanyName());
    }

    @Override
    @CacheEvict(value = ACTIVE_CLIENTS_FOR_SELECT_CACHE, allEntries = true)
    public void toggleClientActive(UUID id) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Client with id " + id + " does not exist"));

        client.setActive(!client.isActive());
        clientRepository.save(client);
        log.info("Client active status toggled: clientId={}, active={}", client.getId(), client.isActive());
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @CacheEvict(value = ACTIVE_CLIENTS_FOR_SELECT_CACHE, allEntries = true)
    public void deleteClient(UUID id) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Client with id " + id + " does not exist"));

        if (invoiceRepository.existsByClientId(id)) {
            throw new BusinessRuleException("Client cannot be deleted because issued invoices already exist");
        }

        if (paymentRepository.existsByClientId(id)) {
            throw new BusinessRuleException("Client cannot be deleted because payments already exist");
        }

        clientRepository.delete(client);
        log.info("Client deleted: clientId={}, displayName={}", client.getId(), client.getDisplayName());
    }

    private ClientSelectDTO mapToClientSelectDTO(Client client) {
        return ClientSelectDTO.builder()
                .id(client.getId())
                .displayName(client.getDisplayName())
                .build();
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
