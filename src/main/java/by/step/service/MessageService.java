package by.step.service;

import by.step.dto.MessageDto;
import by.step.entity.Studio;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface MessageService {

    MessageDto sendMessage(Long orderId, Long senderId, String content);

    MessageDto sendPreview(Long orderId, Long senderId, String content, String attachmentUrl);

    MessageDto sendAttachment(Long orderId, Long senderId, String attachmentUrl);

    Page<MessageDto> getMessagesByOrder(Long orderId, Pageable pageable);

    List<MessageDto> getPreviewMessages(Long orderId);

    List<MessageDto> getMessagesWithAttachments(Long orderId);

    boolean canUserAccessOrderMessages(Long orderId, Long userId);

    long getPreviewMessagesCount(Long orderId);

    Page<MessageDto> getMessagesByStudio(Long studioId, Pageable pageable);

    MessageDto sendToStudio(Long studioId, Long senderId, String content, String attachmentUrl);

    MessageDto sendToArtist(Long studioId, Long senderId, Long receiverId, String content, String attachmentUrl);

    MessageDto redirectToArtist(Long messageId, Long receiverId, Long managerId);

    List<MessageDto> getUnassignedStudioMessages(Long studioId);


}
