package by.step.service.impl;

import by.step.dto.UserDto;
import by.step.entity.ArtistProfile;
import by.step.entity.Profile;
import by.step.entity.User;
import by.step.enums.UserRole;
import by.step.mapper.UserMapper;
import by.step.repository.ArtistProfileRepository;
import by.step.repository.ProfileRepository;
import by.step.repository.UserRepository;
import by.step.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


/**
 * Реализация сервиса для работы с пользователями.
 * Предоставляет операции регистрации, поиска, управления балансом и обновления ролей.
 *
 * @author Skin Market Team
 * @version 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final UserMapper userMapper = UserMapper.INSTANCE;
    private final ArtistProfileRepository artistProfileRepository;


    /**
     * Регистрирует нового пользователя в системе.
     * Создает запись пользователя, профиль и профиль художника если роль ARTIST.
     *
     * @param username уникальное имя пользователя
     * @param email уникальный email пользователя
     * @param password зашифрованный пароль
     * @param role роль пользователя (USER, ARTIST, ADMIN, STUDIO)
     * @return UserDto с данными зарегистрированного пользователя
     * @throws IllegalArgumentException если имя пользователя или email уже заняты
     */
    @Override
    @Transactional
    @CacheEvict(value = {"users", "userByUsername", "userByEmail"}, allEntries = true)
    public UserDto register(String username, String email, String password, UserRole role) {
        log.info("Начало регистрации пользователя: username={}, email={}, role={}", username, email, role);
        long startTime = System.currentTimeMillis();

        try {
            if (userRepository.existsByUsername(username)) {
                log.warn("Регистрация отклонена: имя пользователя {} уже занято", username);
                throw new IllegalArgumentException("Имя пользователя уже занято: " + username);
            }
            if (userRepository.existsByEmail(email)) {
                log.warn("Регистрация отклонена: email {} уже зарегистрирован", email);
                throw new IllegalArgumentException("Email уже зарегистрирован: " + email);
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
            log.debug("Пользователь сохранен в БД с id={}", savedUser.getId());

            createProfileForUser(savedUser, role);

            long elapsedTime = System.currentTimeMillis() - startTime;
            log.info("Регистрация пользователя {} завершена успешно за {} мс", username, elapsedTime);

            return userMapper.toDto(savedUser);

        } catch (Exception e) {
            log.error("Ошибка при регистрации пользователя {}: {}", username, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Создает профиль и (при необходимости) профиль художника для нового пользователя.
     *
     * @param user созданная запись пользователя
     * role роль пользователя
     */
    private void createProfileForUser(User user, UserRole role) {
        log.debug("Создание профиля для пользователя id={}", user.getId());
        Profile profile = Profile.builder()
                .user(user)
                .bio("")
                .isArtist(role == UserRole.ARTIST)
                .isStudio(role == UserRole.STUDIO)
                .build();
        profileRepository.save(profile);
        log.debug("Профиль создан для пользователя: {}", user.getUsername());
    }

    /**
     * Находит пользователя по имени пользователя.
     *
     * @param username имя пользователя для поиска
     * @return Optional с UserDto если найден, иначе пустой Optional
     */
    @Override
    @Cacheable(value = "userByUsername", key = "#username")
    public Optional<UserDto> findByUsername(String username) {
        return userRepository.findByUsername(username)
                .map(userMapper::toDto);
    }

    /**
     * Находит пользователя по ID.
     *
     * @param id ID пользователя для поиска
     * @return Optional с UserDto если найден, иначе пустой Optional
     */
    @Override
    @Cacheable(value = "userById", key = "#id")
    public Optional<UserDto> findById(Long id) {
        return userRepository.findById(id)
                .map(userMapper::toDto);
    }

    /**
     * Находит пользователя по email.
     *
     * @param email email для поиска
     * @return Optional с UserDto если найден, иначе пустой Optional
     */
    @Override
    @Cacheable(value = "userByEmail", key = "#email")
    public Optional<UserDto> findByEmail(String email) {
        return userRepository.findByEmail(email)
                .map(userMapper::toDto);
    }

    /**
     * Возвращает всех пользователей системы.
     *
     * @return список всех UserDto
     */
    @Override
    @Cacheable(value = "users")
    public List<UserDto> findAllUsers() {
        return userRepository.findAll().stream()
                .map(userMapper::toDto)
                .toList();
    }

    /**
     * Находит пользователей по роли.
     *
     * @param role роль для фильтрации
     * @return список UserDto с указанной ролью
     */
    @Override
    @Cacheable(value = "userByRole", key = "#role")
    public List<UserDto> findUserByRole(UserRole role) {
        return userRepository.findByRole(role).stream()
                .map(userMapper::toDto)
                .toList();
    }

    /**
     * Проверяет существование имени пользователя.
     *
     * @param username имя пользователя для проверки
     * @return true если имя существует, false если нет
     */
    @Override
    public boolean existByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    /**
     * Проверяет существование email.
     *
     * @param email email для проверки
     * @return true если email существует, false если нет
     */
    @Override
    public boolean existByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    /**
     * Обновляет баланс пользователя.
     *
     * @param userId ID пользователя
     * @param newBalance новый баланс
     * @throws IllegalArgumentException если новый баланс отрицательный
     */
    @Override
    @Transactional
    @CacheEvict(value = {"userById", "userByUsername", "users"}, key = "#userId")
    public void updateBalance(Long userId, BigDecimal newBalance) {
        if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Баланс не может быть отрицательным");
        }
        userRepository.updateBalance(userId, newBalance);
    }

    /**
     * Добавляет средства на баланс пользователя.
     *
     * @param userId ID пользователя
     * @param amount сумма пополнения
     * @throws IllegalArgumentException если сумма меньше или равна нулю
     */
    @Override
    @Transactional
    @CacheEvict(value = {"userById", "userByUsername", "users"}, key = "#userId")
    public void addToBalance(Long userId, BigDecimal amount) {
        log.info("Пополнение баланса: userId={}, amount={}", userId, amount);

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            log.info("Пополнение отклонено: сумма {} должна быть больше нуля", amount);
            throw new IllegalArgumentException("Сумма пополнения должна быть больше нуля");
        }

        BigDecimal oldBalance = userRepository.findById(userId)
                .map(User::getBalance)
                .orElse(BigDecimal.ZERO);

        userRepository.addToBalance(userId, amount);
        evictUserCache(userId);

        log.info("Баланс пользователя {} изменен: {} -> {}", userId, oldBalance, oldBalance.add(amount));
    }

    /**
     * Списывает средства с баланса пользователя.
     *
     * @param userId ID пользователя
     * @param amount сумма списания
     * @throws IllegalArgumentException если сумма меньше или равна нулю или недостаточно средств
     */
    @Override
    @Transactional
    public void subtractFromBalance(Long userId, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Баланс не может быть отрицательным");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден: " + userId));

        if (user.getBalance().compareTo(amount) < 0) {
            throw new IllegalArgumentException("Недостаточно средств");
        }

        userRepository.subtractFromBalance(userId, amount);
    }

    /**
     * Проверяет достаточно ли средств на балансе.
     *
     * @param userId ID пользователя
     * @param amount требуемая сумма
     * @return true если средств достаточно, false если нет
     */
    @Override
    public boolean hasEnoughBalance(Long userId, BigDecimal amount) {
        return userRepository.findById(userId)
                .map(user -> user.getBalance().compareTo(amount) >= 0)
                .orElse(false);
    }

    /**
     * Обновляет роль пользователя.
     *
     * @param id ID пользователя
     * @param role новая роль
     * @return UserDto с обновленными данными
     * @throws IllegalArgumentException если пользователь не найден или роль неверная
     */
    @Override
    @Transactional
    @CacheEvict(value = {"userById", "userByUsername", "users"}, key = "#id")
    public UserDto updateRole(Long id, String role) {
        log.info("Обновление роли пользователя: id={}, role={}", id, role);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден: " + id));

        UserRole newRole = UserRole.valueOf(role.toUpperCase());
        user.setRole(newRole);
        User saved = userRepository.save(user);

        profileRepository.findByUserId(id).ifPresent(profile -> {
            profile.setIsArtist(newRole == UserRole.ARTIST);
            profile.setIsStudio(newRole == UserRole.STUDIO);
            profileRepository.save(profile);
        });

        return userMapper.toDto(saved);
    }

    /**
     * Находит пользователей по роли с пагинацией.
     *
     * @param role роль для фильтрации
     * @param pageable параметры пагинации
     * @return страница с пользователями указанной роли
     */
    @Override
    public Page<UserDto> findUsersByRolePage(UserRole role, Pageable pageable) {
        log.info("Поиск пользователей по роли {} с пагинацией: page={}, size={}",
                role, pageable.getPageNumber(), pageable.getPageSize());
        return userRepository.findByRole(role, pageable).map(userMapper::toDto);
    }

    /**
     * Возвращает всех пользователей с пагинацией.
     *
     * @param pageable параметры пагинации
     * @return страница со всеми пользователями
     */
    @Override
    public Page<UserDto> findAllUsersPage(Pageable pageable) {
        log.info("Получение всех пользователей с пагинацией: page={}, size={}",
                pageable.getPageNumber(), pageable.getPageSize());
        return userRepository.findAll(pageable).map(userMapper::toDto);
    }

    /**
     * Очищает кэш пользователя.
     *
     * @param userId идентификатор пользователя
     */
    private void evictUserCache(Long userId) {
        log.debug("Очистка кэша для userId: {}", userId);
    }
}