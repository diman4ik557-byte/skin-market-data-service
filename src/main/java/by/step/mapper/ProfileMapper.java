package by.step.mapper;

import by.step.dto.ProfileDto;
import by.step.entity.Profile;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface ProfileMapper {

    ProfileMapper INSTANCE = Mappers.getMapper(ProfileMapper.class);

    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "user.username", target = "username")
    ProfileDto toDto(Profile profile);

    @Mapping(source = "userId", target = "user.id")
    Profile toEntity(ProfileDto profileDto);
}
