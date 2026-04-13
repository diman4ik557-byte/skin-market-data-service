package by.step.mapper;

import by.step.dto.ProfileDto;
import by.step.entity.Profile;
import by.step.entity.User;
import javax.annotation.processing.Generated;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-04-09T21:50:14+0300",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.18 (Ubuntu)"
)
public class ProfileMapperImpl implements ProfileMapper {

    @Override
    public ProfileDto toDto(Profile profile) {
        if ( profile == null ) {
            return null;
        }

        ProfileDto.ProfileDtoBuilder profileDto = ProfileDto.builder();

        profileDto.userId( profileUserId( profile ) );
        profileDto.username( profileUserUsername( profile ) );
        profileDto.id( profile.getId() );
        profileDto.bio( profile.getBio() );
        profileDto.isArtist( profile.getIsArtist() );
        profileDto.isStudio( profile.getIsStudio() );

        return profileDto.build();
    }

    @Override
    public Profile toEntity(ProfileDto profileDto) {
        if ( profileDto == null ) {
            return null;
        }

        Profile.ProfileBuilder profile = Profile.builder();

        profile.user( profileDtoToUser( profileDto ) );
        profile.id( profileDto.getId() );
        profile.bio( profileDto.getBio() );
        profile.isArtist( profileDto.getIsArtist() );
        profile.isStudio( profileDto.getIsStudio() );

        return profile.build();
    }

    private Long profileUserId(Profile profile) {
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

    private String profileUserUsername(Profile profile) {
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

    protected User profileDtoToUser(ProfileDto profileDto) {
        if ( profileDto == null ) {
            return null;
        }

        User.UserBuilder user = User.builder();

        user.id( profileDto.getUserId() );

        return user.build();
    }
}
