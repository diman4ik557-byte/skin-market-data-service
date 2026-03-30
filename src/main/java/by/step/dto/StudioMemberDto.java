package by.step.dto;

import by.step.entity.enums.StudioRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudioMemberDto {

    private Long id;
    private Long studioId;
    private String studioName;
    private Long artistId;
    private String artistName;
    private StudioRole role;
    private LocalDateTime joinedAt;
}
