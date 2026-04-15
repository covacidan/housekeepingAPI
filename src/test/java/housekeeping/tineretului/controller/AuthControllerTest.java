package housekeeping.tineretului.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import housekeeping.tineretului.dto.UserRequest;
import housekeeping.tineretului.dto.UserResponse;
import housekeeping.tineretului.service.KeycloakAdminService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
    controllers = AuthController.class,
    excludeAutoConfiguration = {
        SecurityAutoConfiguration.class,
        SecurityFilterAutoConfiguration.class,
        OAuth2ResourceServerAutoConfiguration.class
    }
)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private KeycloakAdminService keycloakAdminService;

    @Test
    void getUsers_returnsUserList() throws Exception {
        when(keycloakAdminService.findAll()).thenReturn(List.of(
                new UserResponse("uuid-1", "admin@test.com", "ADMIN"),
                new UserResponse("uuid-2", "user@test.com",  "RECORDER")
        ));

        mockMvc.perform(get("/auth/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].email").value("admin@test.com"))
                .andExpect(jsonPath("$[0].role").value("ADMIN"))
                .andExpect(jsonPath("$[1].email").value("user@test.com"));
    }

    @Test
    void createUser_returns201WithUserResponse() throws Exception {
        UserRequest req = new UserRequest();
        req.setEmail("new@test.com");
        req.setPassword("secret");
        req.setRole("RECORDER");

        when(keycloakAdminService.createUser(any())).thenReturn(
                new UserResponse("uuid-3", "new@test.com", "RECORDER"));

        mockMvc.perform(post("/auth/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("new@test.com"))
                .andExpect(jsonPath("$.role").value("RECORDER"));
    }

    @Test
    void deleteUser_returns204() throws Exception {
        doNothing().when(keycloakAdminService).deleteUser("uuid-1");

        mockMvc.perform(delete("/auth/users/uuid-1"))
                .andExpect(status().isNoContent());

        verify(keycloakAdminService).deleteUser("uuid-1");
    }
}
