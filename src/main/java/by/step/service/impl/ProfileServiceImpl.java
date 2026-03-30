package by.step.service.impl;

import by.step.dto.ProfileDto;
import by.step.entity.Profile;
import by.step.entity.User;
import by.step.repository.ProfileRepository;
import by.step.repository.UserRepository;
import by.step.service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProfileServiceImpl implements ProfileService {

    private final ProfileRepository profileRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public ProfileDto createProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден - " +userId));

        if (profileRepository.findByUserId(userId).isPresent()) {
            throw new IllegalArgumentException("Профиль пользователя уже сущетсвует - " +userId);
        }

        Profile profile = Profile.builder()
                .user(user)
                .bio("")
                .isArtist(false)
                .isStudio(false)
                .build();

        Profile savedProfile = profileRepository.save(profile);
        return mapToDto(savedProfile);
    }

    @Override
    public Optional<ProfileDto> findByUserId(Long userId) {
        return profileRepository.findByUserId(userId)
                .map(this::mapToDto);
    }

    @Override
    @Transactional
    public ProfileDto updateBio(Long profileId, String bio) {
        Profile profile = profileRepository.findById(profileId)
                .orElseThrow(() -> new IllegalArgumentException("Профиль не найден - " + profileId));

        profile.setBio(bio);
        Profile updatedProfile = profileRepository.save(profile);
        return mapToDto(updatedProfile);
    }

    @Override
    @Transactional
    public void setArtistStatus(Long profileId, boolean isArtist) {
        Profile profile = profileRepository.findById(profileId)
                .orElseThrow(() -> new IllegalArgumentException("Профиль не найден - " + profileId));

        profile.setIsArtist(isArtist);
        profileRepository.save(profile);
    }

    @Override
    @Transactional
    public void setStudioStatus(Long profileId, boolean isStudio) {
        Profile profile = profileRepository.findById(profileId)
                .orElseThrow(() -> new IllegalArgumentException("Профиль не найден - " + profileId));

        profile.setIsStudio(isStudio);
        profileRepository.save(profile);
    }

    @Override
    public boolean isArtist(Long userId) {
        return profileRepository.findByUserId(userId)
                .map(Profile::getIsArtist)
                .orElse(false);
    }

    @Override
    public boolean isStudio(Long userId) {
        return profileRepository.findByUserId(userId)
                .map(Profile::getIsStudio)
                .orElse(false);
    }

    private ProfileDto mapToDto(Profile profile) {
        return ProfileDto.builder()
                .id(profile.getId())
                .userId(profile.getUser().getId())
                .username(profile.getUser().getUsername())
                .bio(profile.getBio())
                .isArtist(profile.getIsArtist())
                .isStudio(profile.getIsStudio())
                .build();
    }
}
