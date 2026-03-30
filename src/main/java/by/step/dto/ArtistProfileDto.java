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
public class ArtistProfileDto {

    private Long id;
    private Long profileId;
    private String username;
    private String avatarUrl;
    private Long studioId;
    private String studioName;
    private String styles;
    private BigDecimal minPrice;
    private Integer averageTime;
    private Boolean isAvailable;
    private List<String> socialLinks;

    public static ArtistProfileDto of(Long id, Long profileId, String username, String studioName,
                                      String styles, BigDecimal minPrice,
                                      Integer averageTime, Boolean isAvailable) {
        return ArtistProfileDto.builder()
                .id(id)
                .profileId(profileId)
                .username(username)
                .studioName(studioName)
                .styles(styles)
                .minPrice(minPrice)
                .averageTime(averageTime)
                .isAvailable(isAvailable)
                .build();
    }
}
