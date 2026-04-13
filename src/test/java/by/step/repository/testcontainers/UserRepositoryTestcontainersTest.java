package by.step.repository.testcontainers;

import by.step.DataServiceApplication;
import by.step.entity.User;
import by.step.entity.enums.UserRole;
import by.step.repository.UserRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@SpringBootTest(classes = DataServiceApplication.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
class UserRepositoryTestcontainersTest extends PostgresTestcontainersBase {

    @Autowired
    private UserRepository userRepository;

    @Test
    void findByUsername() {
        Optional<User> found = userRepository.findByUsername("ivan");
        Assertions.assertThat(found).isPresent();
        Assertions.assertThat(found.get().getUsername()).isEqualTo("ivan");
    }

    @Test
    void findByEmail() {
        Optional<User> found = userRepository.findByEmail("petr@example.com");
        Assertions.assertThat(found).isPresent();
        Assertions.assertThat(found.get().getEmail()).isEqualTo("petr@example.com");
    }

    @Test
    void checkExistsByUsername() {
        boolean exists = userRepository.existsByUsername("ivan");
        boolean notExists = userRepository.existsByUsername("nonexistent");
        Assertions.assertThat(exists).isTrue();
        Assertions.assertThat(notExists).isFalse();
    }

    @Test
    void findByRole() {
        List<User> artists = userRepository.findByRole(UserRole.ARTIST);
        Assertions.assertThat(artists).isNotEmpty();
        Assertions.assertThat(artists)
                .extracting(User::getRole)
                .containsOnly(UserRole.ARTIST);
    }

    @Test
    @Transactional
    void checkUpdateBalance() {
        userRepository.updateBalance(2L, BigDecimal.valueOf(10000));
        Optional<User> updated = userRepository.findById(2L);
        Assertions.assertThat(updated).isPresent();
        Assertions.assertThat(updated.get().getBalance()).isEqualByComparingTo(BigDecimal.valueOf(10000));
    }

    @Test
    @Transactional
    void checkAddToBalance() {
        userRepository.addToBalance(2L, BigDecimal.valueOf(500));
        Optional<User> updated = userRepository.findById(2L);
        Assertions.assertThat(updated).isPresent();
        Assertions.assertThat(updated.get().getBalance()).isEqualByComparingTo(BigDecimal.valueOf(5500));
    }

    @Test
    @Transactional
    void checkSubtractFromBalance() {
        userRepository.subtractFromBalance(2L, BigDecimal.valueOf(300));
        Optional<User> updated = userRepository.findById(2L);
        Assertions.assertThat(updated).isPresent();
        Assertions.assertThat(updated.get().getBalance()).isEqualByComparingTo(BigDecimal.valueOf(4700));
    }
}