package housekeeping.tineretului.config;

import housekeeping.tineretului.model.Role;
import housekeeping.tineretului.model.User;
import housekeeping.tineretului.repository.UserRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (userRepository.findByEmail("covacidan@gmail.com").isEmpty()) {
            User admin = new User();
            admin.setEmail("covacidan@gmail.com");
            admin.setPassword(passwordEncoder.encode("!buc314COV"));
            admin.setRole(Role.ADMIN);
            userRepository.save(admin);
        }
    }
}
