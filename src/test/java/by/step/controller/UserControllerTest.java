package by.step.controller;

import by.step.dto.UserDto;
import by.step.entity.enums.UserRole;
import by.step.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@ActiveProfiles("test")
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    private UserDto skinMakerDto;

    @BeforeEach
    void setUp() {
        skinMakerDto = UserDto.builder()
                .id(1L)
                .username("skinMaker")
                .email("test@example.com")
                .role(UserRole.USER)
                .balance(BigDecimal.valueOf(1000))
                .registeredAt(LocalDateTime.now())
                .build();
    }

    @Test
    void checkRegister() throws Exception {
        when(userService.register(anyString(), anyString(), anyString(), any(UserRole.class)))
                .thenReturn(skinMakerDto);

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
    void checkFindByUsername() throws Exception {
        when(userService.findByUsername("skinMaker")).thenReturn(Optional.of(skinMakerDto));

        mockMvc.perform(get("/api/users/username/{username}", "skinMaker"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("skinMaker"));
    }

    @Test
    void checkFindByUsernameNotFound() throws Exception {
        when(userService.findByUsername("nonexistent")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/users/username/{username}", "nonexistent"))
                .andExpect(status().isNotFound());
    }

    @Test
    void checkFindAll() throws Exception {
        when(userService.findAllUsers()).thenReturn(List.of(skinMakerDto));

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].username").value("skinMaker"));
    }

    @Test
    void checkGetBalance() throws Exception {
        when(userService.findByUsername("1")).thenReturn(Optional.of(skinMakerDto));

        mockMvc.perform(get("/api/users/{id}/balance", 1L))
                .andExpect(status().isOk())
                .andExpect(content().string("1000"));
    }

    @Test
    void checkAddToBalance() throws Exception {
        doNothing().when(userService).addToBalance(anyLong(), any(BigDecimal.class));

        mockMvc.perform(post("/api/users/{id}/balance/add", 1L)
                        .param("amount", "500"))
                .andExpect(status().isOk());

        verify(userService, times(1)).addToBalance(1L, BigDecimal.valueOf(500));
    }

    @Test
    void checkExistsByUsername() throws Exception {
        when(userService.existByUsername("skinMaker")).thenReturn(true);

        mockMvc.perform(get("/api/users/exists/username/{username}", "skinMaker"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    void checkExistsByEmail() throws Exception {
        when(userService.existByEmail("test@example.com")).thenReturn(true);

        mockMvc.perform(get("/api/users/exists/email/{email}", "test@example.com"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }
}
