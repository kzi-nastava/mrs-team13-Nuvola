package Nuvola.Projekatsiit2025.repositories;

import Nuvola.Projekatsiit2025.model.RegisteredUser;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class UserRepositoryTestEmbedded {

    @Autowired
    private UserRepository userRepository;

    @Test
    void findByEmailIn_returnsMatchingUsers() {

        RegisteredUser u1 = new RegisteredUser();
        u1.setEmail("a@mail.com");
        u1.setUsername("a@mail.com");
        u1.setPassword("pass");
        u1.setFirstName("Ime");
        u1.setLastName("Prezime");
        u1.setAddress("Adr");
        u1.setPhone("000");
        u1.setBlocked(false);
        u1.setActivated(true);

        RegisteredUser u2 = new RegisteredUser();
        u2.setEmail("b@mail.com");
        u2.setUsername("b@mail.com");
        u2.setPassword("pass");
        u2.setFirstName("Ime");
        u2.setLastName("Prezime");
        u2.setAddress("Adr");
        u2.setPhone("000");
        u2.setBlocked(false);
        u2.setActivated(true);

        userRepository.saveAll(List.of(u1, u2));

        var result = userRepository.findByEmailIn(List.of("a@mail.com"));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getEmail()).isEqualTo("a@mail.com");
    }

    @Test
    void findByEmailIn_returnsEmpty_whenNoMatch() {
        var result = userRepository.findByEmailIn(List.of("nepostoji@mail.com"));
        assertThat(result).isEmpty();
    }

    @Test
    void findByEmailIn_returnsEmpty_whenListEmpty() {
        var result = userRepository.findByEmailIn(List.of());
        assertThat(result).isEmpty();
    }
}