package housekeeping.tineretului.service;

import housekeeping.tineretului.dto.UserRequest;
import housekeeping.tineretului.dto.UserResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@Service
public class KeycloakAdminService {

    private static final String ADMIN_CLI     = "admin-cli";
    private static final String ADMIN_REALMS  = "/admin/realms/";
    private static final String USERS_PATH    = "/users/";
    private static final String RECORDER_ROLE = "RECORDER";
    private static final String EMAIL_FIELD   = "email";

    private static final ParameterizedTypeReference<Map<String, Object>> MAP_TYPE =
            new ParameterizedTypeReference<>() {};
    private static final ParameterizedTypeReference<List<Map<String, Object>>> LIST_MAP_TYPE =
            new ParameterizedTypeReference<>() {};

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${keycloak.admin.url}")
    private String keycloakUrl;

    @Value("${keycloak.admin.realm:housekeeping}")
    private String realm;

    @Value("${keycloak.admin.username}")
    private String adminUsername;

    @Value("${keycloak.admin.password}")
    private String adminPassword;

    private String adminRealmPath() {
        return keycloakUrl + ADMIN_REALMS + realm;
    }

    private String getAdminToken() {
        String tokenUrl = keycloakUrl + "/realms/master/protocol/openid-connect/token";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        String body = "grant_type=password&client_id=" + ADMIN_CLI
                + "&username=" + adminUsername
                + "&password=" + adminPassword;

        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                tokenUrl, HttpMethod.POST, new HttpEntity<>(body, headers), MAP_TYPE);

        Map<String, Object> responseBody = response.getBody();
        if (!response.getStatusCode().is2xxSuccessful() || responseBody == null) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to authenticate with Keycloak");
        }
        return (String) responseBody.get("access_token");
    }

    private HttpHeaders bearer(String token) {
        HttpHeaders h = new HttpHeaders();
        h.setBearerAuth(token);
        h.setContentType(MediaType.APPLICATION_JSON);
        return h;
    }

    private String getUserRole(String userId, String token) {
        String url = adminRealmPath() + USERS_PATH + userId + "/role-mappings/realm";
        ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                url, HttpMethod.GET, new HttpEntity<>(bearer(token)), LIST_MAP_TYPE);

        List<Map<String, Object>> body = response.getBody();
        if (body == null) return RECORDER_ROLE;

        for (Map<String, Object> role : body) {
            String name = (String) role.get("name");
            if ("ADMIN".equals(name) || RECORDER_ROLE.equals(name)) return name;
        }
        return RECORDER_ROLE;
    }

    public List<UserResponse> findAll() {
        String token = getAdminToken();
        String url = adminRealmPath() + "/users?max=200";
        ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                url, HttpMethod.GET, new HttpEntity<>(bearer(token)), LIST_MAP_TYPE);

        List<Map<String, Object>> body = response.getBody();
        if (body == null) return Collections.emptyList();

        List<UserResponse> result = new ArrayList<>();
        for (Map<String, Object> u : body) {
            String id    = (String) u.get("id");
            String email = u.get(EMAIL_FIELD) != null ? (String) u.get(EMAIL_FIELD) : (String) u.get("username");
            result.add(new UserResponse(id, email, getUserRole(id, token)));
        }
        return result;
    }

    public UserResponse createUser(UserRequest request) {
        String token = getAdminToken();

        Map<String, Object> userRep = new LinkedHashMap<>();
        userRep.put("username", request.getEmail());
        userRep.put(EMAIL_FIELD, request.getEmail());
        userRep.put("enabled", true);
        userRep.put("emailVerified", true);
        userRep.put("credentials", List.of(Map.of(
                "type", "password",
                "value", request.getPassword(),
                "temporary", false)));

        try {
            ResponseEntity<Void> created = restTemplate.postForEntity(
                    adminRealmPath() + "/users",
                    new HttpEntity<>(userRep, bearer(token)), Void.class);

            String location = created.getHeaders().getFirst(HttpHeaders.LOCATION);
            if (location == null) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "User created but ID not returned");
            }
            String userId = location.substring(location.lastIndexOf('/') + 1);

            String roleName = request.getRole().toUpperCase();
            ResponseEntity<Map<String, Object>> roleRep = restTemplate.exchange(
                    adminRealmPath() + "/roles/" + roleName,
                    HttpMethod.GET, new HttpEntity<>(bearer(token)), MAP_TYPE);

            Map<String, Object> roleBody = roleRep.getBody();
            if (roleBody == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Role not found: " + roleName);
            }

            restTemplate.exchange(
                    adminRealmPath() + USERS_PATH + userId + "/role-mappings/realm",
                    HttpMethod.POST,
                    new HttpEntity<>(List.of(roleBody), bearer(token)),
                    Void.class);

            return new UserResponse(userId, request.getEmail(), roleName);

        } catch (HttpClientErrorException.Conflict e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already in use");
        }
    }

    public void deleteUser(String id) {
        String token = getAdminToken();
        try {
            restTemplate.exchange(
                    adminRealmPath() + USERS_PATH + id,
                    HttpMethod.DELETE, new HttpEntity<>(bearer(token)), Void.class);
        } catch (HttpClientErrorException.NotFound e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
    }
}
