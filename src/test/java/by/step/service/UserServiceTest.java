package by.step.service;

import by.step.dto.UserDto;
import by.step.entity.Profile;
import by.step.entity.User;
import by.step.enums.UserRole;
import by.step.repository.ProfileRepository;
import by.step.repository.UserRepository;
import by.step.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Тесты сервиса пользователей")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProfileRepository profileRepository;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;
    private UserDto testUserDto;
    private Profile testProfile;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .password("encodedPassword")
                .role(UserRole.USER)
                .balance(BigDecimal.valueOf(1000))
                .registeredAt(LocalDateTime.now())
                .build();

        testUserDto = UserDto.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .role(UserRole.USER)
                .balance(BigDecimal.valueOf(1000))
                .registeredAt(LocalDateTime.now())
                .build();

        testProfile = Profile.builder()
                .id(1L)
                .user(testUser)
                .bio("")
                .isArtist(false)
                .isStudio(false)
                .build();
    }

    @Test
    @DisplayName("Регистрация нового пользователя - успех")
    void register_Success() {
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(profileRepository.save(any(Profile.class))).thenReturn(testProfile);

        UserDto result = userService.register("testuser",
                "test@example.com", "password123", UserRole.USER);

        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("testuser");
        assertThat(result.getEmail()).isEqualTo("test@example.com");
        assertThat(result.getRole()).isEqualTo(UserRole.USER);
        verify(userRepository, times(1)).save(any(User.class));
        verify(profileRepository, times(1)).save(any(Profile.class));
    }

    @Test
    @DisplayName("Регистрация - имя пользователя уже занято")
    void register_UsernameAlreadyExists() {
        when(userRepository.existsByUsername("testuser")).thenReturn(true);

        assertThatThrownBy(() -> userService.register("testuser",
                "test@example.com", "password123", UserRole.USER))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Имя пользователя уже занято");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Регистрация - email уже зарегистрирован")
    void register_EmailAlreadyExists() {
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.register("testuser",
                "test@example.com", "password123", UserRole.USER))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Email уже зарегистрирован");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Поиск пользователя по имени - успех")
    void findByUsername_Success() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        Optional<UserDto> result = userService.findByUsername("testuser");

        assertThat(result).isPresent();
        assertThat(result.get().getUsername()).isEqualTo("testuser");
    }

    @Test
    @DisplayName("Поиск пользователя по имени - не найден")
    void findByUsername_NotFound() {
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        Optional<UserDto> result = userService.findByUsername("unknown");

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Поиск пользователя по ID - успех")
    void findById_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        Optional<UserDto> result = userService.findById(1L);

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("Пополнение баланса - успех")
    void addToBalance_Success() {
        doNothing().when(userRepository).addToBalance(1L, BigDecimal.valueOf(500));

        userService.addToBalance(1L, BigDecimal.valueOf(500));

        verify(userRepository, times(1)).addToBalance(1L, BigDecimal.valueOf(500));
    }

    @Test
    @DisplayName("Пополнение баланса - сумма меньше или равна нулю")
    void addToBalance_InvalidAmount() {
        assertThatThrownBy(() -> userService.addToBalance(1L, BigDecimal.valueOf(0)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Сумма пополнения должна быть больше нуля");

        assertThatThrownBy(() -> userService.addToBalance(1L, BigDecimal.valueOf(-100)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Сумма пополнения должна быть больше нуля");

        verify(userRepository, never()).addToBalance(anyLong(), any());
    }

    @Test
    @DisplayName("Обновление роли пользователя - успех")
    void updateRole_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        UserDto result = userService.updateRole(1L, "ARTIST");

        assertThat(result).isNotNull();
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Обновление роли - пользователь не найден")
    void updateRole_UserNotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updateRole(999L, "ARTIST"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Пользователь не найден");
    }

    @Test
    @DisplayName("Обновление роли - неверная роль")
    void updateRole_InvalidRole() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        assertThatThrownBy(() -> userService.updateRole(1L, "INVALID_ROLE"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("No enum constant");
    }

    @Test
    @DisplayName("Получение всех пользователей")
    void findAllUsers_Success() {
        when(userRepository.findAll()).thenReturn(java.util.List.of(testUser));

        java.util.List<UserDto> result = userService.findAllUsers();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUsername()).isEqualTo("testuser");
    }

    @Test
    @DisplayName("Проверка существования имени пользователя")
    void existByUsername_Success() {
        when(userRepository.existsByUsername("testuser")).thenReturn(true);

        boolean result = userService.existByUsername("testuser");

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("Проверка достаточности средств")
    void hasEnoughBalance_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        boolean result = userService.hasEnoughBalance(1L, BigDecimal.valueOf(500));

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("Проверка достаточности средств - недостаточно")
    void hasEnoughBalance_Insufficient() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        boolean result = userService.hasEnoughBalance(1L, BigDecimal.valueOf(2000));

        assertThat(result).isFalse();
    }

    // дополнительно

    @Test
    @DisplayName("Списание с баланса ")
    void subtractFromBalance_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        doNothing().when(userRepository).subtractFromBalance(1L, BigDecimal.valueOf(500));

        userService.subtractFromBalance(1L, BigDecimal.valueOf(500));

        verify(userRepository, times(1)).subtractFromBalance(1L, BigDecimal.valueOf(500));
    }

    @Test
    @DisplayName("Списание с баланса - недостаточно средств")
    void subtractFromBalance_InsufficientFunds() {
        testUser.setBalance(BigDecimal.valueOf(100));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        assertThatThrownBy(() -> userService.subtractFromBalance(1L, BigDecimal.valueOf(500)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Недостаточно средств");
    }

    @Test
    @DisplayName("Списание с баланса - отрицательная сумма")
    void subtractFromBalance_NegativeAmount() {
        assertThatThrownBy(() -> userService.subtractFromBalance(1L, BigDecimal.valueOf(-100)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Баланс не может быть отрицательным");
    }

    @Test
    @DisplayName("Поиск пользователей по роли с пагинацией")
    void findUsersByRolePage_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> userPage = new PageImpl<>(List.of(testUser), pageable, 1);
        when(userRepository.findByRole(UserRole.USER, pageable)).thenReturn(userPage);

        Page<UserDto> result = userService.findUsersByRolePage(UserRole.USER, pageable);

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    @DisplayName("Поиск всех пользователей с пагинацией")
    void findAllUsersPage_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> userPage = new PageImpl<>(List.of(testUser), pageable, 1);
        when(userRepository.findAll(pageable)).thenReturn(userPage);

        Page<UserDto> result = userService.findAllUsersPage(pageable);

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    @DisplayName("Обновление баланса ")
    void updateBalance_Success() {
        doNothing().when(userRepository).updateBalance(1L, BigDecimal.valueOf(2000));

        userService.updateBalance(1L, BigDecimal.valueOf(2000));

        verify(userRepository, times(1)).updateBalance(1L, BigDecimal.valueOf(2000));
    }

    @Test
    @DisplayName("Обновление баланса - отрицательное значение")
    void updateBalance_NegativeBalance() {
        assertThatThrownBy(() -> userService.updateBalance(1L, BigDecimal.valueOf(-100)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Баланс не может быть отрицательным");
    }

    @Test
    @DisplayName("Поиск пользователей по роли")
    void findUserByRole_Success() {
        when(userRepository.findByRole(UserRole.USER)).thenReturn(List.of(testUser));

        List<UserDto> result = userService.findUserByRole(UserRole.USER);

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("Поиск пользователя по email ")
    void findByEmail_Success() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        Optional<UserDto> result = userService.findByEmail("test@example.com");

        assertThat(result).isPresent();
    }

    @Test
    @DisplayName("Поиск пользователя по email - не найден")
    void findByEmail_NotFound() {
        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        Optional<UserDto> result = userService.findByEmail("unknown@example.com");

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Проверка существования email - существует")
    void existByEmail_True() {
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        boolean result = userService.existByEmail("test@example.com");

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("Проверка существования email - не существует")
    void existByEmail_False() {
        when(userRepository.existsByEmail("unknown@example.com")).thenReturn(false);

        boolean result = userService.existByEmail("unknown@example.com");

        assertThat(result).isFalse();
    }
}