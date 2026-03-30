package by.step.dto;

import by.step.entity.enums.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDetailsDto {
    private Long id;
    private String customerName;
    private String artistName;
    private OrderStatus status;
    private String description;
    private BigDecimal price;
    private String finalFileUrl;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
    private List<MessageDto> messages;
    private List<SocialLinkDto> socialLinks;
}
