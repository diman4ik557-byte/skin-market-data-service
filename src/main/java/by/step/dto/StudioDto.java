package by.step.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudioDto {

    private Long id;
    private Long profileId;
    private String name;
    private String description;
    private LocalDate foundedAt;
    private Long managerId;
    private String managerName;
    private Integer membersCount;
    private List<ArtistProfileDto> members;
}
