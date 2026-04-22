package by.step.mapper;

import by.step.dto.SocialLinkDto;
import by.step.entity.Profile;
import by.step.entity.SocialLink;
import by.step.enums.SocialPlatform;
import javax.annotation.processing.Generated;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-04-23T01:27:40+0300",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.18 (Ubuntu)"
)
public class SocialLinkMapperImpl implements SocialLinkMapper {

    @Override
    public SocialLinkDto toDto(SocialLink socialLink) {
        if ( socialLink == null ) {
            return null;
        }

        SocialLinkDto.SocialLinkDtoBuilder socialLinkDto = SocialLinkDto.builder();

        socialLinkDto.profileId( socialLinkProfileId( socialLink ) );
        socialLinkDto.platform( socialLink.getPlatform() );
        socialLinkDto.platformDisplayName( socialLinkPlatformDisplayName( socialLink ) );
        socialLinkDto.id( socialLink.getId() );
        socialLinkDto.userIdentifier( socialLink.getUserIdentifier() );
        socialLinkDto.isPrimary( socialLink.getIsPrimary() );

        socialLinkDto.fullUrl( socialLink.getPlatform().getUrlPrefix() + socialLink.getUserIdentifier() );

        return socialLinkDto.build();
    }

    @Override
    public SocialLink toEntity(SocialLinkDto socialLinkDto) {
        if ( socialLinkDto == null ) {
            return null;
        }

        SocialLink.SocialLinkBuilder socialLink = SocialLink.builder();

        socialLink.profile( socialLinkDtoToProfile( socialLinkDto ) );
        socialLink.id( socialLinkDto.getId() );
        socialLink.platform( socialLinkDto.getPlatform() );
        socialLink.userIdentifier( socialLinkDto.getUserIdentifier() );
        socialLink.isPrimary( socialLinkDto.getIsPrimary() );

        return socialLink.build();
    }

    private Long socialLinkProfileId(SocialLink socialLink) {
        if ( socialLink == null ) {
            return null;
        }
        Profile profile = socialLink.getProfile();
        if ( profile == null ) {
            return null;
        }
        Long id = profile.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }

    private String socialLinkPlatformDisplayName(SocialLink socialLink) {
        if ( socialLink == null ) {
            return null;
        }
        SocialPlatform platform = socialLink.getPlatform();
        if ( platform == null ) {
            return null;
        }
        String displayName = platform.getDisplayName();
        if ( displayName == null ) {
            return null;
        }
        return displayName;
    }

    protected Profile socialLinkDtoToProfile(SocialLinkDto socialLinkDto) {
        if ( socialLinkDto == null ) {
            return null;
        }

        Profile.ProfileBuilder profile = Profile.builder();

        profile.id( socialLinkDto.getProfileId() );

        return profile.build();
    }
}
