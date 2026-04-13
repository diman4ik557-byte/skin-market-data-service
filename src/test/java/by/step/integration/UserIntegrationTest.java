package by.step.integration;

import by.step.DataServiceApplication;
import by.step.dto.UserDto;
import by.step.entity.User;
import by.step.entity.enums.UserRole;
import by.step.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = DataServiceApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class UserIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    void fullUserWorkflow() throws Exception {
        // 1. Регистрация пользователя
        mockMvc.perform(post("/api/users/register")
                        .param("username", "integrationuser")
                        .param("email", "integration@example.com")
                        .param("password", "123")
                        .param("role", "USER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("integrationuser"))
                .andExpect(jsonPath("$.email").value("integration@example.com"))
                .andExpect(jsonPath("$.balance").value(0));

        // 2. Поиск по имени
        mockMvc.perform(get("/api/users/username/{username}", "integrationuser"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.username").value("integrationuser"));

        // 3. Пополнение баланса
        mockMvc.perform(post("/api/users/{id}/balance/add", 1L)
                        .param("amount", "1000"))
                .andExpect(status().isOk());

        // 4. Проверка пополнения
        mockMvc.perform(get("/api/users/{id}/balance", 1L))
                .andExpect(status().isOk())
                .andExpect(content().string("1000"));

        // 5. Проверка существования по имени
        mockMvc.perform(get("/api/users/exists/username/{username}", "integrationuser"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        // 6. Проверка существования по почте
        mockMvc.perform(get("/api/users/exists/email/{email}", "integration@example.com"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    void register_shouldFailWhenUsernameExists() throws Exception {
        // Создание пользователя
        User user = User.builder()
                .username("existinguser")
                .email("existing@example.com")
                .password("123")
                .role(UserRole.USER)
                .balance(BigDecimal.ZERO)
                .build();
        userRepository.save(user);

        // Попытка регистрации с тем же именем
        mockMvc.perform(post("/api/users/register")
                        .param("username", "existinguser")
                        .param("email", "new@example.com")
                        .param("password", "password123")
                        .param("role", "USER"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_shouldFailWhenEmailExists() throws Exception {
        // Создание пользователя
        User user = User.builder()
                .username("newuser")
                .email("existing@example.com")
                .password("123")
                .role(UserRole.USER)
                .balance(BigDecimal.ZERO)
                .build();
        userRepository.save(user);

        // Попытка регистрации с такой же почтой
        mockMvc.perform(post("/api/users/register")
                        .param("username", "differentuser")
                        .param("email", "existing@example.com")
                        .param("password", "password123")
                        .param("role", "USER"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void findById_shouldReturnUser() throws Exception {
        // Создание пользователя
        User user = User.builder()
                .username("findbyid")
                .email("findbyid@example.com")
                .password("password123")
                .role(UserRole.ARTIST)
                .balance(BigDecimal.valueOf(500))
                .build();
        User saved = userRepository.save(user);

        // Поиск по id
        mockMvc.perform(get("/api/users/{id}", saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("findbyid"))
                .andExpect(jsonPath("$.role").value("ARTIST"));
    }

    @Test
    void findById_shouldReturnNotFound() throws Exception {
        mockMvc.perform(get("/api/users/{id}", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void findByRole_shouldReturnUsersWithRole() throws Exception {
        // Создание художника
        User artist1 = User.builder()
                .username("artist_role1")
                .email("artist1@example.com")
                .password("123")
                .role(UserRole.ARTIST)
                .balance(BigDecimal.ZERO)
                .build();
        User artist2 = User.builder()
                .username("artist_role2")
                .email("artist2@example.com")
                .password("123")
                .role(UserRole.ARTIST)
                .balance(BigDecimal.ZERO)
                .build();
        userRepository.save(artist1);
        userRepository.save(artist2);

        mockMvc.perform(get("/api/users/role/{role}", "ARTIST"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }
}