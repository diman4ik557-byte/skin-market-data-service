package by.step.mapper;

import by.step.dto.StudioMemberDto;
import by.step.entity.StudioMember;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface StudioMemberMapper {

    StudioMemberMapper INSTANCE = Mappers.getMapper(StudioMemberMapper.class);

    @Mapping(source = "studio.id", target = "studioId")
    @Mapping(source = "studio.name", target = "studioName")
    @Mapping(source = "member.id", target = "artistId")
    @Mapping(source = "member.profile.user.username", target = "artistName")
    StudioMemberDto toDto(StudioMember studioMember);

    @Mapping(source = "studioId", target = "studio.id")
    @Mapping(source = "artistId", target = "member.id")
    StudioMember toEntity(StudioMemberDto studioMemberDto);
}