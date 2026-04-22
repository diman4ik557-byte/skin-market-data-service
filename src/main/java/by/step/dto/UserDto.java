package by.step.dto;

import by.step.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO для передачи данных о пользователе между слоями приложения.
 * Содержит основную информацию о пользователе: идентификатор, имя, email, роль, баланс.
 *
 * @author Skin Market Team
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {

    /**
     * Уникальный идентификатор пользователя.
     */
    private Long id;

    /**
     * Уникальное имя пользователя (логин).
     */
    private String username;

    /**
     * Email адрес пользователя.
     */
    private String email;

    /**
     * Роль пользователя в системе (USER, ARTIST, STUDIO, ADMIN).
     */
    private UserRole role;

    /**
     * Текущий баланс пользователя (для заказов и выплат).
     */
    private BigDecimal balance;

    /**
     * Дата и время регистрации пользователя.
     */
    private LocalDateTime registeredAt;

    /**
     * Создает UserDto с указанными параметрами.
     *
     * @param id идентификатор
     * @param username имя пользователя
     * @param email email
     * @param role роль
     * @param balance баланс
     * @param registeredAt дата регистрации
     * @return новый экземпляр UserDto
     */
    public static UserDto of(Long id, String username, String email,
                             UserRole role, BigDecimal balance, LocalDateTime registeredAt) {
        return UserDto.builder()
                .id(id)
                .username(username)
                .email(email)
                .role(role)
                .balance(balance)
                .registeredAt(registeredAt)
                .build();
    }
}