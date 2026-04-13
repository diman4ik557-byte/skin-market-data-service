package by.step.mapper;

import by.step.dto.MessageDto;
import by.step.entity.Message;
import by.step.entity.Order;
import by.step.entity.User;
import javax.annotation.processing.Generated;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-04-09T21:50:14+0300",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.18 (Ubuntu)"
)
public class MessageMapperImpl implements MessageMapper {

    @Override
    public MessageDto toDto(Message message) {
        if ( message == null ) {
            return null;
        }

        MessageDto.MessageDtoBuilder messageDto = MessageDto.builder();

        messageDto.orderId( messageOrderId( message ) );
        messageDto.senderId( messageSenderId( message ) );
        messageDto.senderName( messageSenderUsername( message ) );
        messageDto.id( message.getId() );
        messageDto.content( message.getContent() );
        messageDto.attachmentUrl( message.getAttachmentUrl() );
        messageDto.isPreview( message.getIsPreview() );
        messageDto.sentAt( message.getSentAt() );

        return messageDto.build();
    }

    @Override
    public Message toEntity(MessageDto messageDto) {
        if ( messageDto == null ) {
            return null;
        }

        Message.MessageBuilder message = Message.builder();

        message.order( messageDtoToOrder( messageDto ) );
        message.sender( messageDtoToUser( messageDto ) );
        message.id( messageDto.getId() );
        message.content( messageDto.getContent() );
        message.attachmentUrl( messageDto.getAttachmentUrl() );
        message.isPreview( messageDto.getIsPreview() );
        message.sentAt( messageDto.getSentAt() );

        return message.build();
    }

    private Long messageOrderId(Message message) {
        if ( message == null ) {
            return null;
        }
        Order order = message.getOrder();
        if ( order == null ) {
            return null;
        }
        Long id = order.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }

    private Long messageSenderId(Message message) {
        if ( message == null ) {
            return null;
        }
        User sender = message.getSender();
        if ( sender == null ) {
            return null;
        }
        Long id = sender.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }

    private String messageSenderUsername(Message message) {
        if ( message == null ) {
            return null;
        }
        User sender = message.getSender();
        if ( sender == null ) {
            return null;
        }
        String username = sender.getUsername();
        if ( username == null ) {
            return null;
        }
        return username;
    }

    protected Order messageDtoToOrder(MessageDto messageDto) {
        if ( messageDto == null ) {
            return null;
        }

        Order.OrderBuilder order = Order.builder();

        order.id( messageDto.getOrderId() );

        return order.build();
    }

    protected User messageDtoToUser(MessageDto messageDto) {
        if ( messageDto == null ) {
            return null;
        }

        User.UserBuilder user = User.builder();

        user.id( messageDto.getSenderId() );

        return user.build();
    }
}
