package by.step.mapper;

import by.step.dto.ArtistProfileDto;
import by.step.entity.ArtistProfile;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface ArtistProfileMapper {

    ArtistProfileMapper INSTANCE = Mappers.getMapper(ArtistProfileMapper.class);

    @Mapping(source = "profile.user.id", target = "profileId")
    @Mapping(source = "profile.user.username", target = "username")
    @Mapping(source = "studio.name", target = "studioName")
    ArtistProfileDto toDto(ArtistProfile artistProfile);

    ArtistProfile toEntity(ArtistProfileDto dto);
}
