package by.step.service;

import by.step.dto.ArtistProfileDto;
import by.step.dto.StudioDto;

import java.util.List;
import java.util.Optional;

public interface StudioService {

    StudioDto createStudio(Long userId, String name, String description);

    Optional<StudioDto> findById(Long studioId);

    Optional<StudioDto> findByUserId(Long userId);

    List<StudioDto> findAllStudios();

    List<StudioDto> findStudiosByName(String name);

    StudioDto updateDescription(Long studioId, String description);

    void addMember(Long studioId, Long artistId, String role);

    void removeMember(Long studioId, Long artistId);

    void updateMemberRole(Long studioId, Long artistId, String role);

    List<ArtistProfileDto> getStudioMembers(Long studioId);

    long getMemberCount(Long studioId);

    void deleteStudio(Long studioId);
}
