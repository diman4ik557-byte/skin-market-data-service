package by.step.mapper;

import by.step.dto.SocialLinkDto;
import by.step.entity.SocialLink;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface SocialLinkMapper {

    SocialLinkMapper INSTANCE = Mappers.getMapper(SocialLinkMapper.class);

    @Mapping(source = "profile.id", target = "profileId")
    @Mapping(source = "platform", target = "platform")
    @Mapping(source = "platform.displayName", target = "platformDisplayName")
    @Mapping(target = "fullUrl", expression = "java(socialLink.getPlatform().getUrlPrefix() + socialLink.getUserIdentifier())")
    SocialLinkDto toDto(SocialLink socialLink);

    @Mapping(source = "profileId", target = "profile.id")
    SocialLink toEntity(SocialLinkDto socialLinkDto);
}