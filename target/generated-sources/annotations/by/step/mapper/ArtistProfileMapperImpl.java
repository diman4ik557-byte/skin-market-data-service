package by.step.mapper;

import by.step.dto.ArtistProfileDto;
import by.step.entity.ArtistProfile;
import by.step.entity.Profile;
import by.step.entity.Studio;
import by.step.entity.User;
import javax.annotation.processing.Generated;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-04-09T21:50:14+0300",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.18 (Ubuntu)"
)
public class ArtistProfileMapperImpl implements ArtistProfileMapper {

    @Override
    public ArtistProfileDto toDto(ArtistProfile artistProfile) {
        if ( artistProfile == null ) {
            return null;
        }

        ArtistProfileDto.ArtistProfileDtoBuilder artistProfileDto = ArtistProfileDto.builder();

        artistProfileDto.profileId( artistProfileProfileUserId( artistProfile ) );
        artistProfileDto.username( artistProfileProfileUserUsername( artistProfile ) );
        artistProfileDto.studioName( artistProfileStudioName( artistProfile ) );
        artistProfileDto.id( artistProfile.getId() );
        artistProfileDto.styles( artistProfile.getStyles() );
        artistProfileDto.minPrice( artistProfile.getMinPrice() );
        artistProfileDto.averageTime( artistProfile.getAverageTime() );
        artistProfileDto.isAvailable( artistProfile.getIsAvailable() );

        return artistProfileDto.build();
    }

    @Override
    public ArtistProfile toEntity(ArtistProfileDto dto) {
        if ( dto == null ) {
            return null;
        }

        ArtistProfile.ArtistProfileBuilder artistProfile = ArtistProfile.builder();

        artistProfile.id( dto.getId() );
        artistProfile.styles( dto.getStyles() );
        artistProfile.minPrice( dto.getMinPrice() );
        artistProfile.averageTime( dto.getAverageTime() );
        artistProfile.isAvailable( dto.getIsAvailable() );

        return artistProfile.build();
    }

    private Long artistProfileProfileUserId(ArtistProfile artistProfile) {
        if ( artistProfile == null ) {
            return null;
        }
        Profile profile = artistProfile.getProfile();
        if ( profile == null ) {
            return null;
        }
        User user = profile.getUser();
        if ( user == null ) {
            return null;
        }
        Long id = user.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }

    private String artistProfileProfileUserUsername(ArtistProfile artistProfile) {
        if ( artistProfile == null ) {
            return null;
        }
        Profile profile = artistProfile.getProfile();
        if ( profile == null ) {
            return null;
        }
        User user = profile.getUser();
        if ( user == null ) {
            return null;
        }
        String username = user.getUsername();
        if ( username == null ) {
            return null;
        }
        return username;
    }

    private String artistProfileStudioName(ArtistProfile artistProfile) {
        if ( artistProfile == null ) {
            return null;
        }
        Studio studio = artistProfile.getStudio();
        if ( studio == null ) {
            return null;
        }
        String name = studio.getName();
        if ( name == null ) {
            return null;
        }
        return name;
    }
}
