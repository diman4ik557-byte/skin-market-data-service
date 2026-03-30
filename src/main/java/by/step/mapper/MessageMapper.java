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
    @Mapping(source = "sender.id", target = "senderId")
    @Mapping(source = "sender.username", target = "senderName")
    MessageDto toDto(Message message);

    @Mapping(source = "orderId", target = "order.id")
    @Mapping(source = "senderId", target = "sender.id")
    Message toEntity(MessageDto messageDto);
}
