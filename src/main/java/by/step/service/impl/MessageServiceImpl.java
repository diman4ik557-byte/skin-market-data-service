package by.step.service.impl;

import by.step.dto.MessageDto;
import by.step.entity.Message;
import by.step.entity.Order;
import by.step.entity.User;
import by.step.repository.MessageRepository;
import by.step.repository.OrderRepository;
import by.step.repository.UserRepository;
import by.step.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MessageServiceImpl implements MessageService {

    private final MessageRepository messageRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public MessageDto sendMessage(Long orderId, Long senderId, String content) {
        return sendMessageInternal(orderId, senderId, content, null, false);
    }

    @Override
    @Transactional
    public MessageDto sendPreview(Long orderId, Long senderId, String content, String attachmentUrl) {
        return sendMessageInternal(orderId, senderId, content, attachmentUrl, true);
    }

    @Override
    @Transactional
    public MessageDto sendAttachment(Long orderId, Long senderId, String attachmentUrl) {
        return sendMessageInternal(orderId, senderId, null, attachmentUrl, false);
    }

    @Override
    public Page<MessageDto> getMessagesByOrder(Long orderId, Pageable pageable) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Заказ не найден - " + orderId));

        return messageRepository.findByOrder(order, pageable)
                .map(this::mapToDto);
    }

    @Override
    public List<MessageDto> getPreviewMessages(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Заказ не найден - " + orderId));

        return messageRepository.findByOrderAndIsPreviewTrue(order).stream()
                .map(this::mapToDto)
                .toList();
    }

    @Override
    public List<MessageDto> getMessagesWithAttachments(Long orderId) {
        return messageRepository.findMessagesWithAttachments(orderId).stream()
                .map(this::mapToDto)
                .toList();
    }

    @Override
    public boolean canUserAccessOrderMessages(Long orderId, Long userId) {
        Order order = orderRepository.findById(orderId).orElse(null);
        if (order == null) {
            return false;
        }

        return order.getCustomer().getId().equals(userId) ||
                order.getArtist().getId().equals(userId);
    }

    @Override
    public long getPreviewMessagesCount(Long orderId) {
        return messageRepository.countPreviewMessages(orderId);
    }

    @Transactional
    private MessageDto sendMessageInternal(Long orderId, Long senderId, String content,
                                           String attachmentUrl, boolean isPreview) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Заказ не найден - " + orderId));

        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден - " + senderId));

        if (!order.getCustomer().getId().equals(senderId) && !order.getArtist().getId().equals(senderId)) {
            throw new SecurityException("Пользователь вне заказа");
        }

        Message message = Message.builder()
                .order(order)
                .sender(sender)
                .content(content)
                .attachmentUrl(attachmentUrl)
                .isPreview(isPreview)
                .sentAt(LocalDateTime.now())
                .build();

        Message saved = messageRepository.save(message);
        return mapToDto(saved);
    }

    private MessageDto mapToDto(Message message) {
        return MessageDto.builder()
                .id(message.getId())
                .orderId(message.getOrder().getId())
                .senderId(message.getSender().getId())
                .senderName(message.getSender().getUsername())
                .content(message.getContent())
                .attachmentUrl(message.getAttachmentUrl())
                .isPreview(message.getIsPreview())
                .sentAt(message.getSentAt())
                .build();
    }
}
