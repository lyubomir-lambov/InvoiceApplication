package bg.softuni.invoiceapplication.mapper.client;

import bg.softuni.invoiceapplication.model.dto.clients.ClientCreateRequestDTO;
import bg.softuni.invoiceapplication.model.dto.clients.ClientEditRequestDTO;
import bg.softuni.invoiceapplication.model.dto.clients.ClientShowAllDTO;
import bg.softuni.invoiceapplication.model.entity.Client;
import bg.softuni.invoiceapplication.model.enums.Country;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ClientMapperTest {

    private static final UUID CLIENT_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");

    private ClientMapper clientMapper;

    @BeforeEach
    void setUp() {
        clientMapper = new ClientMapper();
    }

    @Test
    void fromClientCreateRequestDTOToClient_shouldMapFields_whenRequestIsValid() {
        ClientCreateRequestDTO requestDTO = createClientCreateRequestDTO();

        Client result = clientMapper.fromClientCreateRequestDTOToClient(requestDTO);

        assertThat(result.getDisplayName()).isEqualTo("Test Client");
        assertThat(result.getCompanyName()).isEqualTo("Test Company Ltd.");
        assertThat(result.getLegalRepresentative()).isEqualTo("Ivan Ivanov");
        assertThat(result.getEmail()).isEqualTo("client@example.com");
        assertThat(result.getPhoneNumber()).isEqualTo("+359888123456");
        assertThat(result.getCountry()).isEqualTo(Country.BULGARIA);
        assertThat(result.getAddress()).isEqualTo("Sofia, Bulgaria");
        assertThat(result.isVatRegistered()).isTrue();
        assertThat(result.getVatNumber()).isEqualTo("BG123456789");
    }

    @Test
    void fromClientCreateRequestDTOToClient_shouldReturnNull_whenRequestIsNull() {
        Client result = clientMapper.fromClientCreateRequestDTOToClient(null);

        assertThat(result).isNull();
    }

    @Test
    void fromAllClientsToClientsShowAllDTO_shouldMapClients_whenClientsExist() {
        Client client = createClient();

        List<ClientShowAllDTO> result = clientMapper.fromAllClientsToClientsShowAllDTO(List.of(client));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(CLIENT_ID);
        assertThat(result.get(0).getDisplayName()).isEqualTo("Test Client");
        assertThat(result.get(0).getCompanyName()).isEqualTo("Test Company Ltd.");
        assertThat(result.get(0).getCountry()).isEqualTo(Country.BULGARIA);
        assertThat(result.get(0).isVatRegistered()).isTrue();
        assertThat(result.get(0).isActive()).isTrue();
    }

    @Test
    void fromAllClientsToClientsShowAllDTO_shouldReturnNull_whenClientsAreNull() {
        List<ClientShowAllDTO> result = clientMapper.fromAllClientsToClientsShowAllDTO(null);

        assertThat(result).isNull();
    }

    @Test
    void fromClientToClientEditRequestDTO_shouldMapFields_whenClientIsValid() {
        Client client = createClient();

        ClientEditRequestDTO result = clientMapper.fromClientToClientEditRequestDTO(client);

        assertThat(result.getId()).isEqualTo(CLIENT_ID);
        assertThat(result.getDisplayName()).isEqualTo("Test Client");
        assertThat(result.getCompanyName()).isEqualTo("Test Company Ltd.");
        assertThat(result.getEmail()).isEqualTo("client@example.com");
        assertThat(result.getCountry()).isEqualTo(Country.BULGARIA);
        assertThat(result.getVatRegistered()).isTrue();
    }

    @Test
    void fromClientToClientEditRequestDTO_shouldReturnNull_whenClientIsNull() {
        ClientEditRequestDTO result = clientMapper.fromClientToClientEditRequestDTO(null);

        assertThat(result).isNull();
    }

    @Test
    void updateClientFromEditRequestDTO_shouldUpdateClientFields_whenRequestIsValid() {
        Client client = createClient();
        ClientEditRequestDTO requestDTO = ClientEditRequestDTO.builder()
                .id(CLIENT_ID)
                .displayName("Updated Client")
                .companyName("Updated Company Ltd.")
                .legalRepresentative("Petar Petrov")
                .email("updated@example.com")
                .phoneNumber("+359899999999")
                .country(Country.GREECE)
                .address("Athens, Greece")
                .vatRegistered(false)
                .vatNumber(null)
                .build();

        clientMapper.updateClientFromEditRequestDTO(client, requestDTO);

        assertThat(client.getDisplayName()).isEqualTo("Updated Client");
        assertThat(client.getCompanyName()).isEqualTo("Updated Company Ltd.");
        assertThat(client.getLegalRepresentative()).isEqualTo("Petar Petrov");
        assertThat(client.getEmail()).isEqualTo("updated@example.com");
        assertThat(client.getPhoneNumber()).isEqualTo("+359899999999");
        assertThat(client.getCountry()).isEqualTo(Country.GREECE);
        assertThat(client.getAddress()).isEqualTo("Athens, Greece");
        assertThat(client.isVatRegistered()).isFalse();
        assertThat(client.getVatNumber()).isNull();
    }

    private ClientCreateRequestDTO createClientCreateRequestDTO() {
        return ClientCreateRequestDTO.builder()
                .displayName("Test Client")
                .companyName("Test Company Ltd.")
                .legalRepresentative("Ivan Ivanov")
                .email("client@example.com")
                .phoneNumber("+359888123456")
                .country(Country.BULGARIA)
                .address("Sofia, Bulgaria")
                .vatRegistered(true)
                .vatNumber("BG123456789")
                .build();
    }

    private Client createClient() {
        Client client = Client.builder()
                .displayName("Test Client")
                .companyName("Test Company Ltd.")
                .legalRepresentative("Ivan Ivanov")
                .email("client@example.com")
                .phoneNumber("+359888123456")
                .country(Country.BULGARIA)
                .address("Sofia, Bulgaria")
                .vatRegistered(true)
                .vatNumber("BG123456789")
                .active(true)
                .build();
        ReflectionTestUtils.setField(client, "id", CLIENT_ID);
        return client;
    }
}
