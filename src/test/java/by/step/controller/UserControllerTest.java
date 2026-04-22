package by.step.controller;

import by.step.dto.UserDto;
import by.step.enums.UserRole;
import by.step.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@DisplayName("Web тесты контроллера пользователей")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    private UserDto testUserDto;

    @BeforeEach
    void setUp() {
        testUserDto = UserDto.builder()
                .id(1L)
                .username("skinMaker")
                .email("test@example.com")
                .role(UserRole.USER)
                .balance(BigDecimal.valueOf(1000))
                .registeredAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Регистрация пользователя")
    void register_Success() throws Exception {
        when(userService.register(anyString(), anyString(), anyString(), any(UserRole.class)))
                .thenReturn(testUserDto);

        mockMvc.perform(post("/api/users/register")
                        .param("username", "skinMaker")
                        .param("email", "test@example.com")
                        .param("password", "123")
                        .param("role", "USER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("skinMaker"));
    }

    @Test
    @DisplayName("Поиск пользователя по имени")
    void findByUsername_Success() throws Exception {
        when(userService.findByUsername("skinMaker")).thenReturn(Optional.of(testUserDto));

        mockMvc.perform(get("/api/users/username/{username}", "skinMaker"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("skinMaker"));
    }

    @Test
    @DisplayName("Поиск пользователя по имени - не найден")
    void findByUsername_NotFound() throws Exception {
        when(userService.findByUsername("nonexistent")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/users/username/{username}", "nonexistent"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Поиск пользователя по ID")
    void findById_Success() throws Exception {
        when(userService.findById(1L)).thenReturn(Optional.of(testUserDto));

        mockMvc.perform(get("/api/users/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("skinMaker"));
    }

    @Test
    @DisplayName("Поиск пользователя по ID - не найден")
    void findById_NotFound() throws Exception {
        when(userService.findById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/users/{id}", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Поиск пользователя по email")
    void findByEmail_Success() throws Exception {
        when(userService.findByEmail("test@example.com")).thenReturn(Optional.of(testUserDto));

        mockMvc.perform(get("/api/users/email/{email}", "test@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }

    @Test
    @DisplayName("Получение всех пользователей")
    void findAll_Success() throws Exception {
        when(userService.findAllUsers()).thenReturn(List.of(testUserDto));

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].username").value("skinMaker"));
    }

    @Test
    @DisplayName("Поиск пользователей по роли")
    void findByRole_Success() throws Exception {
        when(userService.findUserByRole(UserRole.ARTIST)).thenReturn(List.of(testUserDto));

        mockMvc.perform(get("/api/users/role/{role}", "ARTIST"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    @DisplayName("Получение баланса пользователя")
    void getBalance_Success() throws Exception {
        when(userService.findById(1L)).thenReturn(Optional.of(testUserDto));

        mockMvc.perform(get("/api/users/{id}/balance", 1L))
                .andExpect(status().isOk())
                .andExpect(content().string("1000"));
    }

    @Test
    @DisplayName("Получение баланса пользователя - не найден")
    void getBalance_NotFound() throws Exception {
        when(userService.findById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/users/{id}/balance", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Пополнение баланса")
    void addToBalance_Success() throws Exception {
        doNothing().when(userService).addToBalance(1L, BigDecimal.valueOf(500));

        mockMvc.perform(post("/api/users/{id}/balance/add", 1L)
                        .param("amount", "500"))
                .andExpect(status().isOk());

        verify(userService, times(1)).addToBalance(1L, BigDecimal.valueOf(500));
    }

    @Test
    @DisplayName("Списание с баланса")
    void subtractFromBalance_Success() throws Exception {
        doNothing().when(userService).subtractFromBalance(1L, BigDecimal.valueOf(500));

        mockMvc.perform(post("/api/users/{id}/balance/subtract", 1L)
                        .param("amount", "500"))
                .andExpect(status().isOk());

        verify(userService, times(1)).subtractFromBalance(1L, BigDecimal.valueOf(500));
    }

    @Test
    @DisplayName("Проверка существования имени пользователя - существует")
    void existsByUsername_True() throws Exception {
        when(userService.existByUsername("skinMaker")).thenReturn(true);

        mockMvc.perform(get("/api/users/exists/username/{username}", "skinMaker"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    @DisplayName("Проверка существования имени пользователя - не существует")
    void existsByUsername_False() throws Exception {
        when(userService.existByUsername("nonexistent")).thenReturn(false);

        mockMvc.perform(get("/api/users/exists/username/{username}", "nonexistent"))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
    }

    @Test
    @DisplayName("Проверка существования email - существует")
    void existsByEmail_True() throws Exception {
        when(userService.existByEmail("test@example.com")).thenReturn(true);

        mockMvc.perform(get("/api/users/exists/email/{email}", "test@example.com"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    @DisplayName("Обновление роли пользователя ")
    void updateRole_Success() throws Exception {
        UserDto updatedUser = UserDto.builder()
                .id(1L)
                .username("skinMaker")
                .role(UserRole.ARTIST)
                .build();

        when(userService.updateRole(1L, "ARTIST")).thenReturn(updatedUser);

        mockMvc.perform(put("/api/users/{id}/role", 1L)
                        .param("role", "ARTIST"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("ARTIST"));
    }
}