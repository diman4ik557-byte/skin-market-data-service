package by.step.repository;

import by.step.entity.User;
import by.step.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@Disabled("Testcontainers временно отключены")
@DisplayName("Тесты репозитория пользователей")
class UserRepositoryTest extends PostgresTestcontainersBase {

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .username("testuser_" + System.currentTimeMillis())
                .email("test_" + System.currentTimeMillis() + "@example.com")
                .password("encoded123")
                .role(UserRole.USER)
                .balance(BigDecimal.ZERO)
                .registeredAt(LocalDateTime.now())
                .build();
        testUser = userRepository.save(testUser);
    }

    @Test
    @DisplayName("Сохранение и поиск пользователя")
    void saveAndFindUser() {
        Optional<User> found = userRepository.findById(testUser.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo(testUser.getUsername());
    }

    @Test
    @DisplayName("Поиск пользователя по имени")
    void findByUsername() {
        Optional<User> found = userRepository.findByUsername(testUser.getUsername());
        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo(testUser.getEmail());
    }

    @Test
    @DisplayName("Проверка существования пользователя по имени")
    void existsByUsername() {
        boolean exists = userRepository.existsByUsername(testUser.getUsername());
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Обновление баланса пользователя")
    void updateBalance() {
        userRepository.updateBalance(testUser.getId(), BigDecimal.valueOf(500));

        Optional<User> updated = userRepository.findById(testUser.getId());
        assertThat(updated).isPresent();
        assertThat(updated.get().getBalance()).isEqualByComparingTo(BigDecimal.valueOf(500));
    }

    @Test
    @DisplayName("Поиск пользователей по роли")
    void findByRole() {
        List<User> users = userRepository.findByRole(UserRole.USER);
        assertThat(users).isNotEmpty();
        assertThat(users).extracting(User::getUsername).contains(testUser.getUsername());
    }
}