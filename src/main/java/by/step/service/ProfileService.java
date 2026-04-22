package by.step.service;

import by.step.dto.ProfileDto;

import java.util.Optional;

public interface ProfileService {

    ProfileDto createProfile(Long userId);

    Optional<ProfileDto> findByUserId(Long userId);

    ProfileDto updateBio(Long profileId, String bio);

    void setArtistStatus(Long profileId, boolean isArtist);

    void setStudioStatus(Long profileId, boolean isStudio);

    boolean isArtist(Long userId);

    boolean isStudio(Long userId);

}
