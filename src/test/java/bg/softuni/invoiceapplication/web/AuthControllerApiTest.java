package bg.softuni.invoiceapplication.web;

import bg.softuni.invoiceapplication.model.dto.users.UserLoginRequestDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.hamcrest.Matchers.instanceOf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

class AuthControllerApiTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new AuthController())
                .build();
    }

    @Test
    void loginForm_shouldReturnLoginPageWithUserModel_whenLoginEndpointIsCalled() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("user-login"))
                .andExpect(model().attributeExists("user"))
                .andExpect(model().attribute("user", instanceOf(UserLoginRequestDTO.class)));
    }
}
