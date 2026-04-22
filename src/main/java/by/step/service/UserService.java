package by.step.service;

import by.step.dto.UserDto;
import by.step.enums.UserRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.nio.channels.FileChannel;
import java.util.List;
import java.util.Optional;

public interface UserService {

    UserDto register(String username, String email, String password,
                     UserRole role);

    Optional<UserDto> findByUsername(String username);

    Optional<UserDto> findByEmail(String email);

    List<UserDto> findAllUsers();

    List<UserDto> findUserByRole(UserRole role);

    boolean existByUsername(String username);

    boolean existByEmail(String email);

    void updateBalance(Long userId, BigDecimal newBalance);

    void addToBalance(Long userId,BigDecimal amount);

    void subtractFromBalance(Long userId,BigDecimal amount);

    boolean hasEnoughBalance(Long userId,BigDecimal amount);

    Optional<UserDto> findById(Long id);

    UserDto updateRole(Long id, String role);

    Page<UserDto> findUsersByRolePage(UserRole role, Pageable pageable);

    Page<UserDto> findAllUsersPage(Pageable pageable);
}
