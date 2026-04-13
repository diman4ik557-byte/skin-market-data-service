package by.step.service.impl;

import by.step.dto.SocialLinkDto;
import by.step.entity.Profile;
import by.step.entity.SocialLink;
import by.step.entity.enums.SocialPlatform;
import by.step.mapper.SocialLinkMapper;
import by.step.repository.ProfileRepository;
import by.step.repository.SocialLinkRepository;
import by.step.service.SocialLinkService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SocialLinkServiceImpl implements SocialLinkService {

    private final SocialLinkRepository socialLinkRepository;
    private final ProfileRepository profileRepository;
    private final SocialLinkMapper socialLinkMapper = SocialLinkMapper.INSTANCE;

    @Override
    @Transactional
    public SocialLinkDto addSocialLink(Long profileId, SocialPlatform platform, String userIdentifier) {
        Profile profile = profileRepository.findById(profileId)
                .orElseThrow(() -> new IllegalArgumentException("Профиль не найден - " + profileId));

        if (socialLinkRepository.findByProfileAndPlatform(profile, platform).isPresent()) {
            throw new IllegalArgumentException("Ссыылка на платформу" + platform + " уже указана");
        }

        boolean isPrimary = socialLinkRepository.findByProfile(profile).isEmpty();

        SocialLink link = SocialLink.builder()
                .profile(profile)
                .platform(platform)
                .userIdentifier(userIdentifier)
                .isPrimary(isPrimary)
                .build();

        SocialLink saved = socialLinkRepository.save(link);
        return socialLinkMapper.toDto(saved);
    }

    @Override
    public Optional<SocialLinkDto> findByProfileAndPlatform(Long profileId, SocialPlatform platform) {
        Profile profile = profileRepository.findById(profileId).orElse(null);
        if (profile == null) {
            return Optional.empty();
        }
        return socialLinkRepository.findByProfileAndPlatform(profile, platform)
                .map(socialLinkMapper::toDto);
    }

    @Override
    public List<SocialLinkDto> findByProfile(Long profileId) {
        Profile profile = profileRepository.findById(profileId)
                .orElseThrow(() -> new IllegalArgumentException("Профиль не найден " + profileId));

        return socialLinkRepository.findByProfile(profile).stream()
                .map(socialLinkMapper::toDto)
                .toList();
    }

    @Override
    public List<SocialLinkDto> findPrimaryLinks(Long profileId) {
        Profile profile = profileRepository.findById(profileId)
                .orElseThrow(() -> new IllegalArgumentException("Profile not found: " + profileId));

        return socialLinkRepository.findByProfileAndIsPrimaryTrue(profile)
                .stream()
                .map(socialLinkMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public void setPrimary(Long linkId) {
        SocialLink link = socialLinkRepository.findById(linkId)
                .orElseThrow(() -> new IllegalArgumentException("Ссылка не найдена - " + linkId));

        socialLinkRepository.resetPrimaryFlag(link.getProfile());

        link.setIsPrimary(true);
        socialLinkRepository.save(link);
    }

    @Override
    @Transactional
    public void updateUserIdentifier(Long linkId, String userIdentifier) {
        SocialLink link = socialLinkRepository.findById(linkId)
                .orElseThrow(() -> new IllegalArgumentException("Ссылка не найдена - " + linkId));

        link.setUserIdentifier(userIdentifier);
        socialLinkRepository.save(link);
    }

    @Override
    @Transactional
    public void removeSocialLink(Long linkId) {
        SocialLink link = socialLinkRepository.findById(linkId)
                .orElseThrow(() -> new IllegalArgumentException("Ссылка не найдена - " + linkId));

        socialLinkRepository.delete(link);
    }

    @Override
    @Transactional
    public void removeAllByProfile(Long profileId) {
        Profile profile = profileRepository.findById(profileId)
                .orElseThrow(() -> new IllegalArgumentException("Профиль не найден " + profileId));

        socialLinkRepository.deleteByProfile(profile);
    }

    @Override
    public String getFullUrl(SocialPlatform platform, String userIdentifier) {
        return platform.getUrlPrefix() + userIdentifier;
    }

}
