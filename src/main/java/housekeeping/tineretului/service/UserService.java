package housekeeping.tineretului.service;

import housekeeping.tineretului.dto.UserRequest;
import housekeeping.tineretului.dto.UserResponse;
import housekeeping.tineretului.model.User;

import java.util.List;
import java.util.Optional;

public interface UserService {
    Optional<User> findByEmail(String email);
    List<UserResponse> findAll();
    UserResponse createUser(UserRequest request);
    void deleteUser(Long id);
}
