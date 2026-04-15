package housekeeping.tineretului.service;

import housekeeping.tineretului.dto.UserRequest;
import housekeeping.tineretului.dto.UserResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class KeycloakAdminService {

    private static final String ADMIN_CLI = "admin-cli";

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${keycloak.admin.url}")
    private String keycloakUrl;

    @Value("${keycloak.admin.realm:housekeeping}")
    private String realm;

    @Value("${keycloak.admin.username}")
    private String adminUsername;

    @Value("${keycloak.admin.password}")
    private String adminPassword;

    private String getAdminToken() {
        String tokenUrl = keycloakUrl + "/realms/master/protocol/openid-connect/token";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        String body = "grant_type=password&client_id=" + ADMIN_CLI
                + "&username=" + adminUsername
                + "&password=" + adminPassword;

        ResponseEntity<Map> response = restTemplate.postForEntity(
                tokenUrl, new HttpEntity<>(body, headers), Map.class);

        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to authenticate with Keycloak");
        }
        return (String) response.getBody().get("access_token");
    }

    private HttpHeaders bearer(String token) {
        HttpHeaders h = new HttpHeaders();
        h.setBearerAuth(token);
        h.setContentType(MediaType.APPLICATION_JSON);
        return h;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private String getUserRole(String userId, String token) {
        String url = keycloakUrl + "/admin/realms/" + realm + "/users/" + userId + "/role-mappings/realm";
        ResponseEntity<List> response = restTemplate.exchange(
                url, HttpMethod.GET, new HttpEntity<>(bearer(token)), List.class);

        if (response.getBody() == null) return "RECORDER";

        for (Object raw : response.getBody()) {
            if (!(raw instanceof Map)) continue;
            Map<String, Object> role = (Map<String, Object>) raw;
            String name = (String) role.get("name");
            if ("ADMIN".equals(name) || "RECORDER".equals(name)) return name;
        }
        return "RECORDER";
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public List<UserResponse> findAll() {
        String token = getAdminToken();
        String url = keycloakUrl + "/admin/realms/" + realm + "/users?max=200";
        ResponseEntity<List> response = restTemplate.exchange(
                url, HttpMethod.GET, new HttpEntity<>(bearer(token)), List.class);

        if (response.getBody() == null) return Collections.emptyList();

        List<UserResponse> result = new ArrayList<>();
        for (Object raw : response.getBody()) {
            if (!(raw instanceof Map)) continue;
            Map<String, Object> u = (Map<String, Object>) raw;
            String id    = (String) u.get("id");
            String email = u.get("email") != null ? (String) u.get("email") : (String) u.get("username");
            result.add(new UserResponse(id, email, getUserRole(id, token)));
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    public UserResponse createUser(UserRequest request) {
        String token = getAdminToken();

        Map<String, Object> userRep = new LinkedHashMap<>();
        userRep.put("username", request.getEmail());
        userRep.put("email", request.getEmail());
        userRep.put("enabled", true);
        userRep.put("emailVerified", true);
        userRep.put("credentials", List.of(Map.of(
                "type", "password",
                "value", request.getPassword(),
                "temporary", false)));

        try {
            ResponseEntity<Void> created = restTemplate.postForEntity(
                    keycloakUrl + "/admin/realms/" + realm + "/users",
                    new HttpEntity<>(userRep, bearer(token)), Void.class);

            String location = created.getHeaders().getFirst(HttpHeaders.LOCATION);
            if (location == null) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "User created but ID not returned");
            }
            String userId = location.substring(location.lastIndexOf('/') + 1);

            // Fetch role representation then assign it
            String roleName = request.getRole().toUpperCase();
            ResponseEntity<Map> roleRep = restTemplate.exchange(
                    keycloakUrl + "/admin/realms/" + realm + "/roles/" + roleName,
                    HttpMethod.GET, new HttpEntity<>(bearer(token)), Map.class);

            if (roleRep.getBody() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Role not found: " + roleName);
            }

            restTemplate.exchange(
                    keycloakUrl + "/admin/realms/" + realm + "/users/" + userId + "/role-mappings/realm",
                    HttpMethod.POST,
                    new HttpEntity<>(List.of(roleRep.getBody()), bearer(token)),
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
                    keycloakUrl + "/admin/realms/" + realm + "/users/" + id,
                    HttpMethod.DELETE, new HttpEntity<>(bearer(token)), Void.class);
        } catch (HttpClientErrorException.NotFound e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
    }
}
