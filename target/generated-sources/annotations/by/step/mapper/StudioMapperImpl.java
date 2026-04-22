package by.step.mapper;

import by.step.dto.StudioDto;
import by.step.entity.Profile;
import by.step.entity.Studio;
import by.step.entity.User;
import javax.annotation.processing.Generated;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-04-23T01:27:40+0300",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.18 (Ubuntu)"
)
public class StudioMapperImpl implements StudioMapper {

    @Override
    public StudioDto toDto(Studio studio) {
        if ( studio == null ) {
            return null;
        }

        StudioDto.StudioDtoBuilder studioDto = StudioDto.builder();

        studioDto.profileId( studioProfileId( studio ) );
        studioDto.managerId( studioManagerId( studio ) );
        studioDto.managerName( studioManagerUsername( studio ) );
        studioDto.id( studio.getId() );
        studioDto.name( studio.getName() );
        studioDto.description( studio.getDescription() );
        studioDto.foundedAt( studio.getFoundedAt() );

        return studioDto.build();
    }

    @Override
    public Studio toEntity(StudioDto studioDto) {
        if ( studioDto == null ) {
            return null;
        }

        Studio.StudioBuilder studio = Studio.builder();

        studio.profile( studioDtoToProfile( studioDto ) );
        studio.manager( studioDtoToUser( studioDto ) );
        studio.id( studioDto.getId() );
        studio.name( studioDto.getName() );
        studio.description( studioDto.getDescription() );
        studio.foundedAt( studioDto.getFoundedAt() );

        return studio.build();
    }

    private Long studioProfileId(Studio studio) {
        if ( studio == null ) {
            return null;
        }
        Profile profile = studio.getProfile();
        if ( profile == null ) {
            return null;
        }
        Long id = profile.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }

    private Long studioManagerId(Studio studio) {
        if ( studio == null ) {
            return null;
        }
        User manager = studio.getManager();
        if ( manager == null ) {
            return null;
        }
        Long id = manager.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }

    private String studioManagerUsername(Studio studio) {
        if ( studio == null ) {
            return null;
        }
        User manager = studio.getManager();
        if ( manager == null ) {
            return null;
        }
        String username = manager.getUsername();
        if ( username == null ) {
            return null;
        }
        return username;
    }

    protected Profile studioDtoToProfile(StudioDto studioDto) {
        if ( studioDto == null ) {
            return null;
        }

        Profile.ProfileBuilder profile = Profile.builder();

        profile.id( studioDto.getProfileId() );

        return profile.build();
    }

    protected User studioDtoToUser(StudioDto studioDto) {
        if ( studioDto == null ) {
            return null;
        }

        User.UserBuilder user = User.builder();

        user.id( studioDto.getManagerId() );

        return user.build();
    }
}
