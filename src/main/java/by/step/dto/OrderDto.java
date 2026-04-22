package by.step.dto;

import by.step.enums.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO для передачи данных о заказе между слоями приложения.
 * Содержит информацию о заказе: кто заказал, кто выполняет, статус, цена.
 *
 * @author Skin Market Team
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDto {

    /**
     * Уникальный идентификатор заказа.
     */
    private Long id;

    /**
     * ID заказчика.
     */
    private Long customerId;

    /**
     * Имя заказчика.
     */
    private String customerName;

    /**
     * ID художника, выполняющего заказ.
     */
    private Long artistId;

    /**
     * Имя художника.
     */
    private String artistName;

    /**
     * Текущий статус заказа (NEW, IN_PROGRESS, REVIEW, COMPLETED, CANCELLED).
     */
    private OrderStatus status;

    /**
     * Описание заказа (требования к скину).
     */
    private String description;

    /**
     * Стоимость заказа.
     */
    private BigDecimal price;

    /**
     * URL финального файла (готового скина).
     */
    private String finalFileUrl;

    /**
     * Дата и время создания заказа.
     */
    private LocalDateTime createdAt;

    /**
     * Дата и время завершения заказа.
     */
    private LocalDateTime completedAt;

    /**
     * Создает OrderDto с указанными параметрами.
     *
     * @param id идентификатор заказа
     * @param customerId ID заказчика
     * @param customerName имя заказчика
     * @param artistId ID художника
     * @param artistName имя художника
     * @param status статус заказа
     * @param description описание
     * @param price цена
     * @param finalFileUrl URL финального файла
     * @param createdAt дата создания
     * @param completedAt дата завершения
     * @return новый экземпляр OrderDto
     */
    public static OrderDto of(Long id, Long customerId, String customerName,
                              Long artistId, String artistName,
                              OrderStatus status, String description, BigDecimal price,
                              String finalFileUrl, LocalDateTime createdAt, LocalDateTime completedAt) {
        return OrderDto.builder()
                .id(id)
                .customerId(customerId)
                .customerName(customerName)
                .artistId(artistId)
                .artistName(artistName)
                .status(status)
                .description(description)
                .price(price)
                .finalFileUrl(finalFileUrl)
                .createdAt(createdAt)
                .completedAt(completedAt)
                .build();
    }
}