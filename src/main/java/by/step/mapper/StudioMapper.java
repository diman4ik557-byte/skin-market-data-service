package by.step.mapper;

import by.step.dto.StudioDto;
import by.step.entity.Studio;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface StudioMapper {

    StudioMapper INSTANCE = Mappers.getMapper(StudioMapper.class);

    @Mapping(source = "profile.id", target = "profileId")
    @Mapping(source = "manager.id", target = "managerId")
    @Mapping(source = "manager.username", target = "managerName")
    StudioDto toDto(Studio studio);

    @Mapping(source = "profileId", target = "profile.id")
    @Mapping(source = "managerId", target = "manager.id")
    Studio toEntity(StudioDto studioDto);
}