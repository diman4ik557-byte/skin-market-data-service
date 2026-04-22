package by.step.mapper;

import by.step.dto.MessageDto;
import by.step.entity.Message;
import by.step.entity.Order;
import by.step.entity.Studio;
import by.step.entity.User;
import javax.annotation.processing.Generated;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-04-23T01:27:39+0300",
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
        messageDto.studioId( messageStudioId( message ) );
        messageDto.studioName( messageStudioName( message ) );
        messageDto.senderId( messageSenderId( message ) );
        messageDto.senderName( messageSenderUsername( message ) );
        messageDto.receiverId( messageReceiverId( message ) );
        messageDto.receiverName( messageReceiverUsername( message ) );
        messageDto.isPreview( message.getIsPreview() );
        messageDto.isRedirected( message.getIsRedirected() );
        messageDto.id( message.getId() );
        messageDto.content( message.getContent() );
        messageDto.attachmentUrl( message.getAttachmentUrl() );
        messageDto.sentAt( message.getSentAt() );

        return messageDto.build();
    }

    @Override
    public Message toEntity(MessageDto messageDto) {
        if ( messageDto == null ) {
            return null;
        }

        Message.MessageBuilder message = Message.builder();

        message.id( messageDto.getId() );
        message.content( messageDto.getContent() );
        message.attachmentUrl( messageDto.getAttachmentUrl() );
        message.isPreview( messageDto.getIsPreview() );
        message.isRedirected( messageDto.getIsRedirected() );
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

    private Long messageStudioId(Message message) {
        if ( message == null ) {
            return null;
        }
        Studio studio = message.getStudio();
        if ( studio == null ) {
            return null;
        }
        Long id = studio.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }

    private String messageStudioName(Message message) {
        if ( message == null ) {
            return null;
        }
        Studio studio = message.getStudio();
        if ( studio == null ) {
            return null;
        }
        String name = studio.getName();
        if ( name == null ) {
            return null;
        }
        return name;
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

    private Long messageReceiverId(Message message) {
        if ( message == null ) {
            return null;
        }
        User receiver = message.getReceiver();
        if ( receiver == null ) {
            return null;
        }
        Long id = receiver.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }

    private String messageReceiverUsername(Message message) {
        if ( message == null ) {
            return null;
        }
        User receiver = message.getReceiver();
        if ( receiver == null ) {
            return null;
        }
        String username = receiver.getUsername();
        if ( username == null ) {
            return null;
        }
        return username;
    }
}
