package bg.softuni.invoiceapplication.service.impl;

import bg.softuni.invoiceapplication.exception.BusinessRuleException;
import bg.softuni.invoiceapplication.exception.ResourceNotFoundException;
import bg.softuni.invoiceapplication.mapper.client.ClientMapper;
import bg.softuni.invoiceapplication.model.dto.clients.ClientCreateRequestDTO;
import bg.softuni.invoiceapplication.model.dto.clients.ClientEditRequestDTO;
import bg.softuni.invoiceapplication.model.dto.clients.ClientSelectDTO;
import bg.softuni.invoiceapplication.model.dto.clients.ClientShowAllDTO;
import bg.softuni.invoiceapplication.model.entity.Client;
import bg.softuni.invoiceapplication.model.enums.Country;
import bg.softuni.invoiceapplication.repository.ClientRepository;
import bg.softuni.invoiceapplication.repository.InvoiceRepository;
import bg.softuni.invoiceapplication.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ClientServiceImplTest {

    private static final UUID CLIENT_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID INACTIVE_CLIENT_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");

    private FakeClientRepository fakeClientRepository;
    private FakeInvoiceRepository fakeInvoiceRepository;
    private FakePaymentRepository fakePaymentRepository;
    private ClientServiceImpl clientService;

    @BeforeEach
    void setUp() {
        fakeClientRepository = new FakeClientRepository();
        fakeInvoiceRepository = new FakeInvoiceRepository();
        fakePaymentRepository = new FakePaymentRepository();

        clientService = new ClientServiceImpl(
                fakeClientRepository.repository(),
                fakeInvoiceRepository.repository(),
                fakePaymentRepository.repository(),
                new ClientMapper()
        );
    }

    @Test
    void createClient_shouldSaveClientWithNormalizedVatNumber_whenRequestIsValid() {
        ClientCreateRequestDTO requestDTO = createClientCreateRequestDTO();

        Client result = clientService.createClient(requestDTO);

        assertThat(result.getDisplayName()).isEqualTo("Lambi");
        assertThat(result.getVatNumber()).isEqualTo("BG123456789");
        assertThat(fakeClientRepository.savedClient.getVatNumber()).isEqualTo("BG123456789");
    }

    @Test
    void createClient_shouldThrowException_whenRequestIsNull() {
        assertThatThrownBy(() -> clientService.createClient(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Client create request must not be null");

        assertThat(fakeClientRepository.savedClient).isNull();
    }

    @Test
    void createClient_shouldThrowException_whenDisplayNameExists() {
        fakeClientRepository.displayNameExists = true;
        ClientCreateRequestDTO requestDTO = createClientCreateRequestDTO();

        assertThatThrownBy(() -> clientService.createClient(requestDTO))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Client with display name Lambi already exists");

        assertThat(fakeClientRepository.savedClient).isNull();
    }

    @Test
    void createClient_shouldThrowException_whenVatNumberExists() {
        fakeClientRepository.vatNumberExists = true;
        ClientCreateRequestDTO requestDTO = createClientCreateRequestDTO();

        assertThatThrownBy(() -> clientService.createClient(requestDTO))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Client with VAT number BG123456789 already exists");

        assertThat(fakeClientRepository.savedClient).isNull();
    }

    @Test
    void findAllClients_shouldReturnMappedClientsOrderedByActiveAndDisplayName() {
        fakeClientRepository.addClient(createClient(CLIENT_ID, "Lambi", true));

        List<ClientShowAllDTO> result = clientService.findAllClients();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(CLIENT_ID);
        assertThat(result.get(0).getDisplayName()).isEqualTo("Lambi");
        assertThat(fakeClientRepository.findAllCalled).isTrue();
    }

    @Test
    void findAllActiveClientsForSelect_shouldReturnActiveClients() {
        fakeClientRepository.addClient(createClient(CLIENT_ID, "Lambi", true));
        fakeClientRepository.addClient(createClient(INACTIVE_CLIENT_ID, "Old Client", false));

        List<ClientSelectDTO> result = clientService.findAllActiveClientsForSelect();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(CLIENT_ID);
        assertThat(result.get(0).getDisplayName()).isEqualTo("Lambi");
    }

    @Test
    void findAllActiveClientsForSelect_shouldIncludeSelectedInactiveClient_whenSelectedClientIsMissingFromActiveList() {
        fakeClientRepository.addClient(createClient(CLIENT_ID, "Lambi", true));
        fakeClientRepository.addClient(createClient(INACTIVE_CLIENT_ID, "Old Client", false));

        List<ClientSelectDTO> result = clientService.findAllActiveClientsForSelect(INACTIVE_CLIENT_ID);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(INACTIVE_CLIENT_ID);
        assertThat(result.get(0).getDisplayName()).isEqualTo("Old Client");
        assertThat(result.get(1).getId()).isEqualTo(CLIENT_ID);
    }

    @Test
    void findClientsByName_shouldReturnAllClients_whenNameIsBlank() {
        fakeClientRepository.addClient(createClient(CLIENT_ID, "Lambi", true));

        List<ClientShowAllDTO> result = clientService.findClientsByName("   ");

        assertThat(result).hasSize(1);
        assertThat(fakeClientRepository.findAllCalled).isTrue();
    }

    @Test
    void findClientsByName_shouldSearchByTrimmedName_whenNameIsPresent() {
        fakeClientRepository.searchResult = List.of(createClient(CLIENT_ID, "Lambi", true));

        List<ClientShowAllDTO> result = clientService.findClientsByName("  Lam  ");

        assertThat(result).hasSize(1);
        assertThat(fakeClientRepository.lastSearchDisplayName).isEqualTo("Lam");
        assertThat(fakeClientRepository.lastSearchCompanyName).isEqualTo("Lam");
    }

    @Test
    void getClientForEdit_shouldReturnEditDTO_whenClientExists() {
        fakeClientRepository.addClient(createClient(CLIENT_ID, "Lambi", true));

        ClientEditRequestDTO result = clientService.getClientForEdit(CLIENT_ID);

        assertThat(result.getId()).isEqualTo(CLIENT_ID);
        assertThat(result.getDisplayName()).isEqualTo("Lambi");
    }

    @Test
    void getClientForEdit_shouldThrowException_whenClientDoesNotExist() {
        assertThatThrownBy(() -> clientService.getClientForEdit(CLIENT_ID))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Client with id " + CLIENT_ID + " does not exist");
    }

    @Test
    void findDuplicateFieldForEdit_shouldReturnDisplayName_whenDisplayNameExists() {
        fakeClientRepository.duplicateDisplayNameClient = createClient(INACTIVE_CLIENT_ID, "Lambi", true);
        ClientEditRequestDTO requestDTO = createClientEditRequestDTO();

        Optional<String> result = clientService.findDuplicateFieldForEdit(requestDTO);

        assertThat(result).contains("displayName");
    }

    @Test
    void findDuplicateFieldForEdit_shouldReturnVatNumber_whenVatNumberExists() {
        fakeClientRepository.duplicateVatNumberClient = createClient(INACTIVE_CLIENT_ID, "Other", true);
        ClientEditRequestDTO requestDTO = createClientEditRequestDTO();

        Optional<String> result = clientService.findDuplicateFieldForEdit(requestDTO);

        assertThat(result).contains("vatNumber");
        assertThat(requestDTO.getVatNumber()).isEqualTo("BG123456789");
    }

    @Test
    void editClient_shouldUpdateAndSaveClient_whenRequestIsValid() {
        Client client = createClient(CLIENT_ID, "Old Name", true);
        fakeClientRepository.addClient(client);
        ClientEditRequestDTO requestDTO = createClientEditRequestDTO();

        clientService.editClient(requestDTO);

        assertThat(client.getDisplayName()).isEqualTo("Lambi");
        assertThat(client.getVatNumber()).isEqualTo("BG123456789");
        assertThat(fakeClientRepository.savedClient).isSameAs(client);
    }

    @Test
    void editClient_shouldThrowException_whenClientDoesNotExist() {
        ClientEditRequestDTO requestDTO = createClientEditRequestDTO();

        assertThatThrownBy(() -> clientService.editClient(requestDTO))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Client with id " + CLIENT_ID + " does not exist");

        assertThat(fakeClientRepository.savedClient).isNull();
    }

    @Test
    void toggleClientActive_shouldChangeActiveFlagAndSaveClient_whenClientExists() {
        Client client = createClient(CLIENT_ID, "Lambi", true);
        fakeClientRepository.addClient(client);

        clientService.toggleClientActive(CLIENT_ID);

        assertThat(client.isActive()).isFalse();
        assertThat(fakeClientRepository.savedClient).isSameAs(client);
    }

    @Test
    void deleteClient_shouldDeleteClient_whenClientHasNoInvoicesOrPayments() {
        Client client = createClient(CLIENT_ID, "Lambi", true);
        fakeClientRepository.addClient(client);

        clientService.deleteClient(CLIENT_ID);

        assertThat(fakeClientRepository.deletedClient).isSameAs(client);
    }

    @Test
    void deleteClient_shouldThrowException_whenClientHasInvoices() {
        Client client = createClient(CLIENT_ID, "Lambi", true);
        fakeClientRepository.addClient(client);
        fakeInvoiceRepository.clientHasInvoices = true;

        assertThatThrownBy(() -> clientService.deleteClient(CLIENT_ID))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Client cannot be deleted because issued invoices already exist");

        assertThat(fakeClientRepository.deletedClient).isNull();
    }

    @Test
    void deleteClient_shouldThrowException_whenClientHasPayments() {
        Client client = createClient(CLIENT_ID, "Lambi", true);
        fakeClientRepository.addClient(client);
        fakePaymentRepository.clientHasPayments = true;

        assertThatThrownBy(() -> clientService.deleteClient(CLIENT_ID))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Client cannot be deleted because payments already exist");

        assertThat(fakeClientRepository.deletedClient).isNull();
    }

    private ClientCreateRequestDTO createClientCreateRequestDTO() {
        return ClientCreateRequestDTO.builder()
                .displayName("Lambi")
                .companyName("Lambi Ltd.")
                .legalRepresentative("Lyubomir Lambov")
                .email("client@example.com")
                .phoneNumber("+359888123456")
                .country(Country.BULGARIA)
                .address("Sofia")
                .vatRegistered(true)
                .vatNumber(" bg123456789 ")
                .build();
    }

    private ClientEditRequestDTO createClientEditRequestDTO() {
        return ClientEditRequestDTO.builder()
                .id(CLIENT_ID)
                .displayName("Lambi")
                .companyName("Lambi Ltd.")
                .legalRepresentative("Lyubomir Lambov")
                .email("client@example.com")
                .phoneNumber("+359888123456")
                .country(Country.BULGARIA)
                .address("Sofia")
                .vatRegistered(true)
                .vatNumber(" bg123456789 ")
                .build();
    }

    private Client createClient(UUID id, String displayName, boolean active) {
        Client client = Client.builder()
                .displayName(displayName)
                .companyName(displayName + " Ltd.")
                .legalRepresentative("Lyubomir Lambov")
                .email("client@example.com")
                .phoneNumber("+359888123456")
                .country(Country.BULGARIA)
                .address("Sofia")
                .vatRegistered(true)
                .vatNumber("BG123456789")
                .active(active)
                .build();
        ReflectionTestUtils.setField(client, "id", id);
        return client;
    }

    private static final class FakeClientRepository {

        private final Map<UUID, Client> clients = new LinkedHashMap<>();

        private boolean displayNameExists;
        private boolean vatNumberExists;
        private boolean findAllCalled;
        private String lastSearchDisplayName;
        private String lastSearchCompanyName;
        private List<Client> searchResult = List.of();
        private Client duplicateDisplayNameClient;
        private Client duplicateVatNumberClient;
        private Client savedClient;
        private Client deletedClient;

        private void addClient(Client client) {
            clients.put(client.getId(), client);
        }

        private ClientRepository repository() {
            return proxy(ClientRepository.class, (proxy, method, args) -> switch (method.getName()) {
                case "existsByDisplayName" -> displayNameExists;
                case "existsByVatNumber" -> vatNumberExists;
                case "findAll" -> {
                    findAllCalled = true;
                    yield new ArrayList<>(clients.values());
                }
                case "findAllByActiveTrueOrderByDisplayNameAsc" -> clients.values()
                        .stream()
                        .filter(Client::isActive)
                        .sorted(Comparator.comparing(Client::getDisplayName))
                        .toList();
                case "findById" -> Optional.ofNullable(clients.get((UUID) args[0]));
                case "findByDisplayNameContainingIgnoreCaseOrCompanyNameContainingIgnoreCase" -> {
                    lastSearchDisplayName = (String) args[0];
                    lastSearchCompanyName = (String) args[1];
                    yield searchResult;
                }
                case "findByDisplayNameAndIdNot" -> Optional.ofNullable(duplicateDisplayNameClient);
                case "findByVatNumberAndIdNot" -> Optional.ofNullable(duplicateVatNumberClient);
                case "existsByDisplayNameAndIdNot" -> duplicateDisplayNameClient != null;
                case "existsByVatNumberAndIdNot" -> duplicateVatNumberClient != null;
                case "save" -> {
                    savedClient = (Client) args[0];
                    yield savedClient;
                }
                case "delete" -> {
                    deletedClient = (Client) args[0];
                    yield null;
                }
                default -> defaultValue(method.getReturnType());
            });
        }
    }

    private static final class FakeInvoiceRepository {

        private boolean clientHasInvoices;

        private InvoiceRepository repository() {
            return proxy(InvoiceRepository.class, (proxy, method, args) -> switch (method.getName()) {
                case "existsByClientId" -> clientHasInvoices;
                default -> defaultValue(method.getReturnType());
            });
        }
    }

    private static final class FakePaymentRepository {

        private boolean clientHasPayments;

        private PaymentRepository repository() {
            return proxy(PaymentRepository.class, (proxy, method, args) -> switch (method.getName()) {
                case "existsByClientId" -> clientHasPayments;
                default -> defaultValue(method.getReturnType());
            });
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> T proxy(Class<T> type, InvocationHandler invocationHandler) {
        return (T) Proxy.newProxyInstance(
                type.getClassLoader(),
                new Class<?>[]{type},
                invocationHandler
        );
    }

    private static Object defaultValue(Class<?> returnType) {
        if (returnType.equals(boolean.class)) {
            return false;
        }

        if (returnType.equals(Optional.class)) {
            return Optional.empty();
        }

        if (returnType.equals(List.class)) {
            return List.of();
        }

        return null;
    }
}
