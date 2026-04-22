package by.step.dto;

import by.step.enums.SocialPlatform;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SocialLinkDto {
    private Long id;
    private Long profileId;
    private SocialPlatform platform;
    private String platformDisplayName;
    private String userIdentifier;
    private String fullUrl;
    private Boolean isPrimary;
}
