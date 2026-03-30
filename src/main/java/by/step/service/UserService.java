package by.step.service;

import by.step.dto.UserDto;
import by.step.entity.enums.UserRole;

import java.math.BigDecimal;
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
}
