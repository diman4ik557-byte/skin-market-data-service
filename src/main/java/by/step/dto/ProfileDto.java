package by.step.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileDto {

    private Long id;
    private Long userId;
    private String username;
    private String bio;
    private Boolean isArtist;
    private Boolean isStudio;
}
