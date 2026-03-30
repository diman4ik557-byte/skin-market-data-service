package by.step.service.impl;

import by.step.dto.UserDto;
import by.step.entity.User;
import by.step.entity.enums.UserRole;
import by.step.repository.UserRepository;
import by.step.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import by.step.mapper.UserMapper;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper = UserMapper.INSTANCE;

    @Override
    @Transactional
    public UserDto register(String username, String email, String password, UserRole role) {

        if (userRepository.existsByUsername(username)){
            throw new IllegalArgumentException("Имя \""+username+"\" занято");
        }
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Эта почта уже зарегестрирована");
        }

        User user = User.builder()
                .username(username)
                .email(email)
                .password(password)
                .role(role)
                .balance(BigDecimal.ZERO)
                .registeredAt(LocalDateTime.now())
                .build();

        User savedUser = userRepository.save(user);
        return userMapper.toDto(savedUser);
    }

    @Override
    public Optional<UserDto> findByUsername(String username) {
        return userRepository.findByUsername(username)
                .map(userMapper::toDto);
    }

    @Override
    public Optional<UserDto> findByEmail(String email) {
        return userRepository.findByEmail(email)
                .map(userMapper::toDto);
    }

    @Override
    public List<UserDto> findAllUsers() {
        return userRepository.findAll().stream()
                .map(userMapper::toDto)
                .toList();
    }

    @Override
    public List<UserDto> findUserByRole(UserRole role) {
        return userRepository.findByRole(role).stream()
                .map(userMapper::toDto)
                .toList();
    }

    @Override
    public boolean existByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    @Override
    public boolean existByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    @Transactional
    public void updateBalance(Long userId, BigDecimal newBalance) {
        if(newBalance.compareTo(BigDecimal.ZERO) < 0){
            throw new IllegalArgumentException("Баланс не может быть негативным");
        }
        userRepository.updateBalance(userId,newBalance);
    }

    @Override
    @Transactional
    public void addToBalance(Long userId, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0){
            throw new IllegalArgumentException("Пополнение должно быть больше нуля");
        }
        userRepository.addToBalance(userId, amount);
    }

    @Override
    @Transactional
    public void subtractFromBalance(Long userId, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Баланс не может быть негативным");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден: " + userId));

        if (user.getBalance().compareTo(amount) < 0) {
            throw new IllegalArgumentException("Недостаточно средств");
        }

        userRepository.subtractFromBalance(userId, amount);
    }

    @Override
    public boolean hasEnoughBalance(Long userId, BigDecimal amount) {
        return userRepository.findById(userId)
                .map(user -> user.getBalance().compareTo(amount) >= 0)
                .orElse(false);
    }

}
