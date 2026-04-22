package by.step.mapper;

import by.step.dto.MessageDto;
import by.step.entity.Message;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface MessageMapper {

    MessageMapper INSTANCE = Mappers.getMapper(MessageMapper.class);

    @Mapping(source = "order.id", target = "orderId")
    @Mapping(source = "studio.id", target = "studioId")
    @Mapping(source = "studio.name", target = "studioName")
    @Mapping(source = "sender.id", target = "senderId")
    @Mapping(source = "sender.username", target = "senderName")
    @Mapping(source = "receiver.id", target = "receiverId")
    @Mapping(source = "receiver.username", target = "receiverName")
    @Mapping(source = "isPreview", target = "isPreview")
    @Mapping(source = "isRedirected", target = "isRedirected")
    MessageDto toDto(Message message);

    Message toEntity(MessageDto messageDto);
}