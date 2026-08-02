package bg.softuni.invoiceapplication.web;

import bg.softuni.invoiceapplication.exception.BusinessRuleException;
import bg.softuni.invoiceapplication.model.dto.clients.ClientCreateRequestDTO;
import bg.softuni.invoiceapplication.model.dto.clients.ClientEditRequestDTO;
import bg.softuni.invoiceapplication.model.dto.clients.ClientSelectDTO;
import bg.softuni.invoiceapplication.model.dto.clients.ClientShowAllDTO;
import bg.softuni.invoiceapplication.model.entity.Client;
import bg.softuni.invoiceapplication.model.enums.Country;
import bg.softuni.invoiceapplication.model.enums.UserRole;
import bg.softuni.invoiceapplication.security.AuthenticatedUserDetails;
import bg.softuni.invoiceapplication.service.ClientService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ClientControllerTest {

    private static final UUID CLIENT_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");

    private FakeClientService clientService;
    private ClientController clientController;

    @BeforeEach
    void setUp() {
        clientService = new FakeClientService();
        clientController = new ClientController(clientService);
    }

    @Test
    void clientsShowAll_shouldAddClientsAndClientNameToModel() {
        Model model = new ExtendedModelMap();

        String viewName = clientController.clientsShowAll("Lambi", model);

        assertThat(viewName).isEqualTo("clients");
        assertThat(model.getAttribute("clients")).isEqualTo(clientService.clients);
        assertThat(model.getAttribute("clientName")).isEqualTo("Lambi");
        assertThat(clientService.lastClientName).isEqualTo("Lambi");
    }

    @Test
    void showCreateClientForm_shouldAddClientAndCountriesToModel() {
        Model model = new ExtendedModelMap();

        String viewName = clientController.showCreateClientForm(model);

        assertThat(viewName).isEqualTo("client-create");
        assertThat(model.getAttribute("client")).isInstanceOf(ClientCreateRequestDTO.class);
        assertThat(model.getAttribute("countries")).isEqualTo(Country.values());
    }

    @Test
    void createClient_shouldReturnCreateView_whenBindingResultHasErrors() {
        ClientCreateRequestDTO requestDTO = createClientCreateRequestDTO();
        BindingResult bindingResult = new BeanPropertyBindingResult(requestDTO, "client");
        bindingResult.rejectValue("displayName", "displayName.invalid", "Invalid display name");
        Model model = new ExtendedModelMap();

        String viewName = clientController.createClient(requestDTO, bindingResult, model);

        assertThat(viewName).isEqualTo("client-create");
        assertThat(clientService.createdClientRequest).isNull();
        assertThat(model.getAttribute("countries")).isEqualTo(Country.values());
    }

    @Test
    void createClient_shouldCreateClientAndRedirect_whenRequestIsValid() {
        ClientCreateRequestDTO requestDTO = createClientCreateRequestDTO();
        BindingResult bindingResult = new BeanPropertyBindingResult(requestDTO, "client");
        Model model = new ExtendedModelMap();

        String viewName = clientController.createClient(requestDTO, bindingResult, model);

        assertThat(viewName).isEqualTo("redirect:/clients");
        assertThat(clientService.createdClientRequest).isSameAs(requestDTO);
    }

    @Test
    void editClientGet_shouldAddClientAndCountriesToModel() {
        Model model = new ExtendedModelMap();

        String viewName = clientController.editClient(CLIENT_ID, model);

        assertThat(viewName).isEqualTo("client-edit");
        assertThat(model.getAttribute("client")).isSameAs(clientService.clientForEdit);
        assertThat(model.getAttribute("countries")).isEqualTo(Country.values());
        assertThat(clientService.lastEditClientId).isEqualTo(CLIENT_ID);
    }

    @Test
    void editClientPost_shouldReturnEditView_whenBindingResultHasErrors() {
        ClientEditRequestDTO requestDTO = createClientEditRequestDTO();
        BindingResult bindingResult = new BeanPropertyBindingResult(requestDTO, "client");
        bindingResult.rejectValue("displayName", "displayName.invalid", "Invalid display name");
        Model model = new ExtendedModelMap();

        String viewName = clientController.editClient(CLIENT_ID, requestDTO, bindingResult, model);

        assertThat(viewName).isEqualTo("client-edit");
        assertThat(clientService.editedClientRequest).isNull();
        assertThat(model.getAttribute("countries")).isEqualTo(Country.values());
    }

    @Test
    void editClientPost_shouldReturnEditViewAndRejectDisplayName_whenDuplicateDisplayNameExists() {
        ClientEditRequestDTO requestDTO = createClientEditRequestDTO();
        BindingResult bindingResult = new BeanPropertyBindingResult(requestDTO, "client");
        Model model = new ExtendedModelMap();
        clientService.duplicateField = Optional.of("displayName");

        String viewName = clientController.editClient(CLIENT_ID, requestDTO, bindingResult, model);

        assertThat(viewName).isEqualTo("client-edit");
        assertThat(bindingResult.getFieldError("displayName")).isNotNull();
        assertThat(bindingResult.getFieldError("displayName").getDefaultMessage())
                .isEqualTo("Client with this display name already exists");
        assertThat(clientService.editedClientRequest).isNull();
    }

    @Test
    void editClientPost_shouldReturnEditViewAndRejectVatNumber_whenDuplicateVatNumberExists() {
        ClientEditRequestDTO requestDTO = createClientEditRequestDTO();
        BindingResult bindingResult = new BeanPropertyBindingResult(requestDTO, "client");
        Model model = new ExtendedModelMap();
        clientService.duplicateField = Optional.of("vatNumber");

        String viewName = clientController.editClient(CLIENT_ID, requestDTO, bindingResult, model);

        assertThat(viewName).isEqualTo("client-edit");
        assertThat(bindingResult.getFieldError("vatNumber")).isNotNull();
        assertThat(bindingResult.getFieldError("vatNumber").getDefaultMessage())
                .isEqualTo("Client with this VAT number already exists");
        assertThat(clientService.editedClientRequest).isNull();
    }

    @Test
    void editClientPost_shouldSetIdEditClientAndRedirect_whenRequestIsValid() {
        ClientEditRequestDTO requestDTO = createClientEditRequestDTO();
        BindingResult bindingResult = new BeanPropertyBindingResult(requestDTO, "client");
        Model model = new ExtendedModelMap();

        String viewName = clientController.editClient(CLIENT_ID, requestDTO, bindingResult, model);

        assertThat(viewName).isEqualTo("redirect:/clients");
        assertThat(requestDTO.getId()).isEqualTo(CLIENT_ID);
        assertThat(clientService.editedClientRequest).isSameAs(requestDTO);
    }

    @Test
    void toggleClientStatus_shouldToggleClientAndRedirect() {
        String viewName = clientController.toggleClientStatus(CLIENT_ID);

        assertThat(viewName).isEqualTo("redirect:/clients");
        assertThat(clientService.toggledClientId).isEqualTo(CLIENT_ID);
    }

    @Test
    void deleteClient_shouldRedirectWithMessage_whenCurrentUserIsNull() {
        RedirectAttributesModelMap redirectAttributes = new RedirectAttributesModelMap();

        String viewName = clientController.deleteClient(CLIENT_ID, null, redirectAttributes);

        assertThat(viewName).isEqualTo("redirect:/clients");
        assertThat(clientService.deletedClientId).isNull();
        assertThat(redirectAttributes.getFlashAttributes().get("message")).isEqualTo("Only admins can delete clients");
    }

    @Test
    void deleteClient_shouldRedirectWithMessage_whenCurrentUserIsNotAdmin() {
        RedirectAttributesModelMap redirectAttributes = new RedirectAttributesModelMap();
        AuthenticatedUserDetails user = createUser(UserRole.USER);

        String viewName = clientController.deleteClient(CLIENT_ID, user, redirectAttributes);

        assertThat(viewName).isEqualTo("redirect:/clients");
        assertThat(clientService.deletedClientId).isNull();
        assertThat(redirectAttributes.getFlashAttributes().get("message")).isEqualTo("Only admins can delete clients");
    }

    @Test
    void deleteClient_shouldDeleteClientAndRedirectWithMessage_whenCurrentUserIsAdmin() {
        RedirectAttributesModelMap redirectAttributes = new RedirectAttributesModelMap();
        AuthenticatedUserDetails admin = createUser(UserRole.ADMIN);

        String viewName = clientController.deleteClient(CLIENT_ID, admin, redirectAttributes);

        assertThat(viewName).isEqualTo("redirect:/clients");
        assertThat(clientService.deletedClientId).isEqualTo(CLIENT_ID);
        assertThat(redirectAttributes.getFlashAttributes().get("message")).isEqualTo("Client deleted successfully");
    }

    @Test
    void deleteClient_shouldRedirectWithBusinessRuleMessage_whenServiceThrowsException() {
        RedirectAttributesModelMap redirectAttributes = new RedirectAttributesModelMap();
        AuthenticatedUserDetails admin = createUser(UserRole.ADMIN);
        clientService.deleteException = new BusinessRuleException("Client has invoices");

        String viewName = clientController.deleteClient(CLIENT_ID, admin, redirectAttributes);

        assertThat(viewName).isEqualTo("redirect:/clients");
        assertThat(redirectAttributes.getFlashAttributes().get("message")).isEqualTo("Client has invoices");
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
                .vatNumber("BG123456789")
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
                .vatNumber("BG123456789")
                .build();
    }

    private AuthenticatedUserDetails createUser(UserRole role) {
        return new AuthenticatedUserDetails(UUID.randomUUID(), "user", "password", role, true);
    }

    private static final class FakeClientService implements ClientService {

        private final List<ClientShowAllDTO> clients = List.of(ClientShowAllDTO.builder()
                .id(CLIENT_ID)
                .displayName("Lambi")
                .build());
        private final ClientEditRequestDTO clientForEdit = ClientEditRequestDTO.builder()
                .id(CLIENT_ID)
                .displayName("Lambi")
                .country(Country.BULGARIA)
                .vatRegistered(true)
                .build();

        private String lastClientName;
        private UUID lastEditClientId;
        private ClientCreateRequestDTO createdClientRequest;
        private ClientEditRequestDTO editedClientRequest;
        private Optional<String> duplicateField = Optional.empty();
        private UUID toggledClientId;
        private UUID deletedClientId;
        private BusinessRuleException deleteException;

        @Override
        public Client createClient(ClientCreateRequestDTO clientCreateRequestDTO) {
            createdClientRequest = clientCreateRequestDTO;
            return Client.builder().build();
        }

        @Override
        public List<ClientShowAllDTO> findAllClients() {
            return clients;
        }

        @Override
        public List<ClientSelectDTO> findAllActiveClientsForSelect() {
            return List.of();
        }

        @Override
        public List<ClientSelectDTO> findAllActiveClientsForSelect(UUID selectedClientId) {
            return List.of();
        }

        @Override
        public List<ClientShowAllDTO> findClientsByName(String clientName) {
            lastClientName = clientName;
            return clients;
        }

        @Override
        public ClientEditRequestDTO getClientForEdit(UUID id) {
            lastEditClientId = id;
            return clientForEdit;
        }

        @Override
        public Optional<String> findDuplicateFieldForEdit(ClientEditRequestDTO clientEditRequestDTO) {
            return duplicateField;
        }

        @Override
        public void editClient(ClientEditRequestDTO clientEditRequestDTO) {
            editedClientRequest = clientEditRequestDTO;
        }

        @Override
        public void toggleClientActive(UUID id) {
            toggledClientId = id;
        }

        @Override
        public void deleteClient(UUID id) {
            if (deleteException != null) {
                throw deleteException;
            }
            deletedClientId = id;
        }
    }
}
