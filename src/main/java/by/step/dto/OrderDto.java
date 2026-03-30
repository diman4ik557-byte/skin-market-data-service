package by.step.dto;

import by.step.entity.enums.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDto {

    private Long id;
    private Long customerId;
    private String customerName;
    private Long artistId;
    private String artistName;
    private OrderStatus status;
    private String description;
    private BigDecimal price;
    private String finalFileUrl;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;

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
