package by.step.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArtistDetailsDto {
    private Long id;
    private String username;
    private String bio;
    private String styles;
    private BigDecimal minPrice;
    private Integer averageTime;
    private Boolean isAvailable;
    private StudioDto studio;
    private List<SocialLinkDto> socialLinks;
    private List<OrderDto> recentOrders;
    private Double averageRating;
}
