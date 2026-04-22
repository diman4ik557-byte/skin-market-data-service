package by.step.service;

import by.step.dto.ArtistProfileDto;
import by.step.dto.StudioDto;
import by.step.dto.StudioMemberDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface StudioService {

    StudioDto createStudio(Long userId, String name, String description);

    Optional<StudioDto> findById(Long studioId);

    Optional<StudioDto> findByUserId(Long userId);

    List<StudioDto> findAllStudios();

    List<StudioDto> findStudiosByName(String name);

    StudioDto updateDescription(Long studioId, String description, Long managerId);

    void requestToJoinStudio(Long studioId, Long artistId);

    void approveMember(Long studioId, Long artistId, Long managerId);

    void removeMember(Long studioId, Long artistId, Long managerId);

    List<ArtistProfileDto> getStudioMembers(Long studioId);

    List<StudioMemberDto> getPendingRequests(Long studioId, Long managerId);

    long getMemberCount(Long studioId);

    void deleteStudio(Long studioId, Long managerId);

    boolean isManager(Long userId, Long studioId);

    void leaveStudio(Long studioId, Long artistId);

    Page<StudioDto> findAllStudios(Pageable pageable);

}