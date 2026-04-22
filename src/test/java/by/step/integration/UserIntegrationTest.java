package by.step.integration;

import by.step.DataServiceApplication;
import by.step.config.TestCacheConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest(
        classes = DataServiceApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Интеграционные тесты пользователей")
@Import(TestCacheConfig.class)
class UserIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;


    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.cache.type", () -> "none");
        registry.add("spring.data.redis.repositories.enabled", () -> "false");
    }

    @BeforeEach
    void setUp() {
    }

    @Test
    @DisplayName("Регистрация пользователя - интеграционный тест")
    void registerUser_Success() throws Exception {
        String uniqueSuffix = String.valueOf(System.currentTimeMillis());
        mockMvc.perform(post("/api/users/register")
                        .param("username", "newuser_" + uniqueSuffix)
                        .param("email", "new_" + uniqueSuffix + "@example.com")
                        .param("password", "password123")
                        .param("role", "USER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("newuser_" + uniqueSuffix))
                .andExpect(jsonPath("$.email").value("new_" + uniqueSuffix + "@example.com"))
                .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    @DisplayName("Получение пользователя по имени - интеграционный тест")
    void getUserByUsername_Success() throws Exception {
        String uniqueSuffix = String.valueOf(System.currentTimeMillis());
        String username = "testuser_" + uniqueSuffix;
        String email = "test_" + uniqueSuffix + "@example.com";

        mockMvc.perform(post("/api/users/register")
                .param("username", username)
                .param("email", email)
                .param("password", "pass")
                .param("role", "USER"));

        mockMvc.perform(get("/api/users/username/" + username))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(username));
    }

    @Test
    @DisplayName("Пополнение баланса - интеграционный тест")
    void addToBalance_Success() throws Exception {
        String uniqueSuffix = String.valueOf(System.currentTimeMillis());
        String username = "balanceuser_" + uniqueSuffix;
        String email = "balance_" + uniqueSuffix + "@example.com";

        MvcResult result = mockMvc.perform(post("/api/users/register")
                        .param("username", username)
                        .param("email", email)
                        .param("password", "pass")
                        .param("role", "USER"))
                .andExpect(status().isOk())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        ObjectMapper mapper = new ObjectMapper();
        long userId = mapper.readTree(response).get("id").asLong();

        mockMvc.perform(post("/api/users/" + userId + "/balance/add")
                        .param("amount", "1000"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/users/" + userId + "/balance"))
                .andExpect(status().isOk())
                .andExpect(content().string("1000.00"));
    }

    @Test
    @DisplayName("Обновление роли пользователя - интеграционный тест")
    void updateRole_Success() throws Exception {
        String uniqueSuffix = String.valueOf(System.currentTimeMillis());
        String username = "roleuser_" + uniqueSuffix;
        String email = "role_" + uniqueSuffix + "@example.com";

        MvcResult result = mockMvc.perform(post("/api/users/register")
                        .param("username", username)
                        .param("email", email)
                        .param("password", "pass")
                        .param("role", "USER"))
                .andExpect(status().isOk())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        ObjectMapper mapper = new ObjectMapper();
        long userId = mapper.readTree(response).get("id").asLong();

        mockMvc.perform(put("/api/users/" + userId + "/role")
                        .param("role", "ARTIST"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("ARTIST"));
    }
}