package by.step.mapper;

import by.step.dto.StudioMemberDto;
import by.step.entity.ArtistProfile;
import by.step.entity.Profile;
import by.step.entity.Studio;
import by.step.entity.StudioMember;
import by.step.entity.User;
import javax.annotation.processing.Generated;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-04-23T01:27:40+0300",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.18 (Ubuntu)"
)
public class StudioMemberMapperImpl implements StudioMemberMapper {

    @Override
    public StudioMemberDto toDto(StudioMember studioMember) {
        if ( studioMember == null ) {
            return null;
        }

        StudioMemberDto.StudioMemberDtoBuilder studioMemberDto = StudioMemberDto.builder();

        studioMemberDto.studioId( studioMemberStudioId( studioMember ) );
        studioMemberDto.studioName( studioMemberStudioName( studioMember ) );
        studioMemberDto.artistId( studioMemberMemberId( studioMember ) );
        studioMemberDto.artistName( studioMemberMemberProfileUserUsername( studioMember ) );
        studioMemberDto.id( studioMember.getId() );
        studioMemberDto.role( studioMember.getRole() );
        studioMemberDto.joinedAt( studioMember.getJoinedAt() );

        return studioMemberDto.build();
    }

    @Override
    public StudioMember toEntity(StudioMemberDto studioMemberDto) {
        if ( studioMemberDto == null ) {
            return null;
        }

        StudioMember.StudioMemberBuilder studioMember = StudioMember.builder();

        studioMember.studio( studioMemberDtoToStudio( studioMemberDto ) );
        studioMember.member( studioMemberDtoToArtistProfile( studioMemberDto ) );
        studioMember.id( studioMemberDto.getId() );
        studioMember.role( studioMemberDto.getRole() );
        studioMember.joinedAt( studioMemberDto.getJoinedAt() );

        return studioMember.build();
    }

    private Long studioMemberStudioId(StudioMember studioMember) {
        if ( studioMember == null ) {
            return null;
        }
        Studio studio = studioMember.getStudio();
        if ( studio == null ) {
            return null;
        }
        Long id = studio.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }

    private String studioMemberStudioName(StudioMember studioMember) {
        if ( studioMember == null ) {
            return null;
        }
        Studio studio = studioMember.getStudio();
        if ( studio == null ) {
            return null;
        }
        String name = studio.getName();
        if ( name == null ) {
            return null;
        }
        return name;
    }

    private Long studioMemberMemberId(StudioMember studioMember) {
        if ( studioMember == null ) {
            return null;
        }
        ArtistProfile member = studioMember.getMember();
        if ( member == null ) {
            return null;
        }
        Long id = member.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }

    private String studioMemberMemberProfileUserUsername(StudioMember studioMember) {
        if ( studioMember == null ) {
            return null;
        }
        ArtistProfile member = studioMember.getMember();
        if ( member == null ) {
            return null;
        }
        Profile profile = member.getProfile();
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

    protected Studio studioMemberDtoToStudio(StudioMemberDto studioMemberDto) {
        if ( studioMemberDto == null ) {
            return null;
        }

        Studio.StudioBuilder studio = Studio.builder();

        studio.id( studioMemberDto.getStudioId() );

        return studio.build();
    }

    protected ArtistProfile studioMemberDtoToArtistProfile(StudioMemberDto studioMemberDto) {
        if ( studioMemberDto == null ) {
            return null;
        }

        ArtistProfile.ArtistProfileBuilder artistProfile = ArtistProfile.builder();

        artistProfile.id( studioMemberDto.getArtistId() );

        return artistProfile.build();
    }
}
