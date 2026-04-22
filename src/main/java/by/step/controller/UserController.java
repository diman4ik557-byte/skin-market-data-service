package by.step.controller;

import by.step.dto.UserDto;
import by.step.enums.UserRole;
import by.step.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * REST контроллер для работы с пользователями.
 * Предоставляет API для регистрации, поиска, управления балансом и ролями пользователей.
 *
 * @author Skin Market Team
 * @version 1.0
 */
@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;


    /**
     * Регистрирует нового пользователя.
     *
     * @param username имя пользователя
     * @param email email пользователя
     * @param password пароль
     * @param role роль пользователя (USER, ARTIST, ADMIN, STUDIO)
     * @return зарегистрированный пользователь
     */
    @PostMapping("/register")
    public ResponseEntity<UserDto> register(
            @RequestParam String username,
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam UserRole role) {
        log.info("REST запрос: регистрация пользователя username={}, role={}", username, role);
        UserDto user = userService.register(username, email, password, role);
        return ResponseEntity.ok(user);
    }

    /**
     * Находит пользователя по ID.
     *
     * @param id ID пользователя
     * @return пользователь, если найден, иначе 404
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserDto> findById(@PathVariable Long id) {
        log.debug("REST запрос: поиск пользователя по id={}", id);
        return userService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Находит пользователя по имени пользователя.
     *
     * @param username имя пользователя
     * @return пользователь, если найден, иначе 404
     */
    @GetMapping("/username/{username}")
    public ResponseEntity<UserDto> findByUsername(@PathVariable String username) {
        log.debug("REST запрос: поиск пользователя по username={}", username);
        return userService.findByUsername(username)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Находит пользователя по email.
     *
     * @param email email пользователя
     * @return пользователь, если найден, иначе 404
     */
    @GetMapping("/email/{email}")
    public ResponseEntity<UserDto> findByEmail(@PathVariable String email) {
        log.debug("REST запрос: поиск пользователя по email={}", email);
        return userService.findByEmail(email)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Возвращает список всех пользователей.
     *
     * @return список всех пользователей
     */
    @GetMapping
    public ResponseEntity<List<UserDto>> findAll() {
        log.debug("REST запрос: получение всех пользователей");
        List<UserDto> users = userService.findAllUsers();
        return ResponseEntity.ok(users);
    }

    /**
     * Находит пользователей по роли.
     *
     * @param role роль пользователя
     * @return список пользователей с указанной ролью
     */
    @GetMapping("/role/{role}")
    public ResponseEntity<List<UserDto>> findByRole(@PathVariable UserRole role) {
        log.debug("REST запрос: поиск пользователей по роли {}", role);
        List<UserDto> users = userService.findUserByRole(role);
        return ResponseEntity.ok(users);
    }

    /**
     * Проверяет существование имени пользователя.
     *
     * @param username имя пользователя
     * @return true если существует, false если нет
     */
    @GetMapping("/exists/username/{username}")
    public ResponseEntity<Boolean> existsByUsername(@PathVariable String username) {
        log.debug("REST запрос: проверка существования username={}", username);
        return ResponseEntity.ok(userService.existByUsername(username));
    }

    /**
     * Проверяет существование email.
     *
     * @param email email пользователя
     * @return true если существует, false если нет
     */
    @GetMapping("/exists/email/{email}")
    public ResponseEntity<Boolean> existsByEmail(@PathVariable String email) {
        log.debug("REST запрос: проверка существования email={}", email);
        return ResponseEntity.ok(userService.existByEmail(email));
    }


    /**
     * Возвращает баланс пользователя.
     *
     * @param id ID пользователя
     * @return баланс пользователя
     */
    @GetMapping("/{id}/balance")
    public ResponseEntity<BigDecimal> getBalance(@PathVariable Long id) {
        log.debug("REST запрос: получение баланса пользователя {}", id);
        return userService.findById(id)
                .map(user -> ResponseEntity.ok(user.getBalance()))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Добавляет средства на баланс пользователя.
     *
     * @param id ID пользователя
     * @param amount сумма пополнения
     * @return пустой ответ при успехе
     */
    @PostMapping("/{id}/balance/add")
    public ResponseEntity<Void> addToBalance(
            @PathVariable Long id,
            @RequestParam BigDecimal amount) {
        log.info("REST запрос: пополнение баланса пользователя {} на {}", id, amount);
        userService.addToBalance(id, amount);
        return ResponseEntity.ok().build();
    }

    /**
     * Списывает средства с баланса пользователя.
     *
     * @param id ID пользователя
     * @param amount сумма списания
     * @return пустой ответ при успехе
     */
    @PostMapping("/{id}/balance/subtract")
    public ResponseEntity<Void> subtractFromBalance(
            @PathVariable Long id,
            @RequestParam BigDecimal amount) {
        log.info("REST запрос: списание с баланса пользователя {} суммы {}", id, amount);
        userService.subtractFromBalance(id, amount);
        return ResponseEntity.ok().build();
    }


    /**
     * Обновляет роль пользователя.
     *
     * @param id ID пользователя
     * @param role новая роль (USER, ARTIST, ADMIN, STUDIO)
     * @return обновленный пользователь
     */
    @PutMapping("/{id}/role")
    public ResponseEntity<UserDto> updateRole(
            @PathVariable Long id,
            @RequestParam String role) {
        log.info("REST запрос: обновление роли пользователя {} на {}", id, role);
        UserDto user = userService.updateRole(id, role);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/role/{role}/page")
    public ResponseEntity<Page<UserDto>> getUsersByRolePage(
            @PathVariable UserRole role,
            Pageable pageable) {
        log.debug("REST запрос: получение пользователей по роли {} с пагинацией", role);
        Page<UserDto> users = userService.findUsersByRolePage(role, pageable);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/page")
    public ResponseEntity<Page<UserDto>> getAllUsersPage(Pageable pageable) {
        log.debug("REST запрос: получение всех пользователей с пагинацией");
        Page<UserDto> users = userService.findAllUsersPage(pageable);
        return ResponseEntity.ok(users);
    }
}