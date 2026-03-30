package by.step.service;

import by.step.dto.SocialLinkDto;
import by.step.entity.enums.SocialPlatform;

import java.util.List;
import java.util.Optional;

public interface SocialLinkService {

    SocialLinkDto addSocialLink(Long profileId, SocialPlatform platform, String userIdentifier);

    Optional<SocialLinkDto> findByProfileAndPlatform(Long profileId, SocialPlatform platform);

    List<SocialLinkDto> findByProfile(Long profileId);

    List<SocialLinkDto> findPrimaryLinks(Long profileId);

    void setPrimary(Long linkId);

    void updateUserIdentifier(Long linkId, String userIdentifier);

    void removeSocialLink(Long linkId);

    void removeAllByProfile(Long profileId);

    String getFullUrl(SocialPlatform platform, String userIdentifier);
}
