package by.step.service;

import by.step.dto.UserDto;
import by.step.entity.User;
import by.step.entity.enums.UserRole;
import by.step.repository.UserRepository;
import by.step.service.impl.UserServiceImpl;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceMockitoTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void register_savesEntity() {
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);

        User user = User.builder()
                .username("testuser")
                .email("test@example.com")
                .password("123")
                .role(UserRole.USER)
                .balance(BigDecimal.ZERO)
                .registeredAt(LocalDateTime.now())
                .build();

        when(userRepository.save(any(User.class))).thenReturn(user);

        UserDto result = userService.register("testuser", "test@example.com", "password123",
                UserRole.USER);

        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result.getUsername()).isEqualTo("testuser");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void register_throwsWhenUsernameExists() {
        when(userRepository.existsByUsername("testuser")).thenReturn(true);

        Assertions.assertThatThrownBy(() -> userService.register("testuser", "test@example.com",
                        "password123", UserRole.USER))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("занято");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void findByUsername_returnsRepositoryResult() {
        User user = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .role(UserRole.USER)
                .balance(BigDecimal.valueOf(1000))
                .build();

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        Optional<UserDto> result = userService.findByUsername("testuser");

        Assertions.assertThat(result).isPresent();
        Assertions.assertThat(result.get().getUsername()).isEqualTo("testuser");
        verify(userRepository).findByUsername("testuser");
    }

    @Test
    void findByUsername_returnsEmptyWhenNotFound() {
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        Optional<UserDto> result = userService.findByUsername("nonexistent");

        Assertions.assertThat(result).isEmpty();
        verify(userRepository).findByUsername("nonexistent");
    }

    @Test
    void findAllUsers_delegatesToRepository() {
        List<User> users = List.of(
                User.builder().id(1L).username("user1").build(),
                User.builder().id(2L).username("user2").build()
        );
        when(userRepository.findAll()).thenReturn(users);

        List<UserDto> result = userService.findAllUsers();

        Assertions.assertThat(result).hasSize(2);
        verify(userRepository).findAll();
    }

    @Test
    void addToBalance_delegatesToRepository() {
        doNothing().when(userRepository).addToBalance(anyLong(), any(BigDecimal.class));

        userService.addToBalance(1L, BigDecimal.valueOf(50));

        verify(userRepository, times(1)).addToBalance(1L, BigDecimal.valueOf(50));
    }

    @Test
    void subtractFromBalance_throwsWhenInsufficientFunds() {

        User user = User.builder().id(1L).balance(BigDecimal.valueOf(100)).build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        Assertions.assertThatThrownBy(() -> userService.subtractFromBalance(1L, BigDecimal.valueOf(200)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Недостаточно средств");

        verify(userRepository, never()).subtractFromBalance(anyLong(), any(BigDecimal.class));
    }

    @Test
    void hasEnoughBalance_returnsTrueWhenSufficient() {
        User user = User.builder().id(1L).balance(BigDecimal.valueOf(100)).build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        boolean result = userService.hasEnoughBalance(1L, BigDecimal.valueOf(50));

        Assertions.assertThat(result).isTrue();
    }

    @Test
    void hasEnoughBalance_returnsFalseWhenInsufficient() {
        User user = User.builder().id(1L).balance(BigDecimal.valueOf(100)).build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        boolean result = userService.hasEnoughBalance(1L, BigDecimal.valueOf(200));

        Assertions.assertThat(result).isFalse();
    }
}