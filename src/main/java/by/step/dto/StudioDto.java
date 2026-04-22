package by.step.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/**
 * DTO для передачи данных о студии художников.
 * Содержит информацию о студии: название, описание, основатель, участники.
 *
 * @author Skin Market Team
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudioDto {

    /**
     * Уникальный идентификатор студии.
     */
    private Long id;

    /**
     * ID профиля, связанного со студией.
     */
    private Long profileId;

    /**
     * Название студии.
     */
    private String name;

    /**
     * Описание студии.
     */
    private String description;

    /**
     * Дата основания студии.
     */
    private LocalDate foundedAt;

    /**
     * ID менеджера студии (основателя).
     */
    private Long managerId;

    /**
     * Имя менеджера студии.
     */
    private String managerName;

    /**
     * Количество участников студии.
     */
    private Integer membersCount;

    /**
     * Список участников студии.
     */
    private List<ArtistProfileDto> members;
}