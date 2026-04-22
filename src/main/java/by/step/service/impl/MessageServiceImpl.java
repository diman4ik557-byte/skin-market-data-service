package by.step.service.impl;

import by.step.dto.MessageDto;
import by.step.entity.*;
import by.step.enums.StudioRole;
import by.step.mapper.MessageMapper;
import by.step.repository.*;
import by.step.service.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Реализация сервиса для работы с сообщениями.
 * Обеспечивает обмен сообщениями между участниками заказов и студий,
 * включая отправку предпросмотров, файлов и перенаправление сообщений.
 *
 * @author Skin Market Team
 * @version 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MessageServiceImpl implements MessageService {

    private final MessageRepository messageRepository;
    private final OrderRepository orderRepository;
    private final StudioRepository studioRepository;
    private final UserRepository userRepository;
    private final MessageMapper messageMapper = MessageMapper.INSTANCE;
    private final StudioMemberRepository studioMemberRepository;
    private final ArtistProfileRepository artistProfileRepository;

    /**
     * Отправляет обычное сообщение в чат заказа.
     *
     * @param orderId ID заказа
     * @param senderId ID отправителя
     * @param content текст сообщения
     * @return MessageDto отправленного сообщения
     * @throws IllegalArgumentException если заказ или отправитель не найдены
     * @throws SecurityException если отправитель не участвует в заказе
     */
    @Override
    @Transactional
    public MessageDto sendMessage(Long orderId, Long senderId, String content) {
        log.info("Отправка сообщения в заказ: orderId={}, senderId={}", orderId, senderId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Заказ не найден: " + orderId));

        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден: " + senderId));

        if (!order.getCustomer().getId().equals(senderId) && !order.getArtist().getId().equals(senderId)) {
            throw new SecurityException("Пользователь не участвует в этом заказе");
        }

        Message message = Message.builder()
                .order(order)
                .sender(sender)
                .content(content)
                .isPreview(false)
                .sentAt(LocalDateTime.now())
                .build();

        return messageMapper.toDto(messageRepository.save(message));
    }


    /**
     * Отправляет предпросмотр работы в чат заказа.
     *
     * @param orderId ID заказа
     * @param senderId ID отправителя
     * @param content описание предпросмотра
     * @param attachmentUrl URL изображения предпросмотра
     * @return MessageDto отправленного предпросмотра
     * @throws IllegalArgumentException если заказ или отправитель не найдены
     * @throws SecurityException если отправитель не участвует в заказе
     */
    @Override
    @Transactional
    public MessageDto sendPreview(Long orderId, Long senderId, String content, String attachmentUrl) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Заказ не найден - " + orderId));
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден - " + senderId));

        Message message = Message.builder()
                .order(order)
                .sender(sender)
                .content(content)
                .attachmentUrl(attachmentUrl)
                .isPreview(true)
                .sentAt(LocalDateTime.now())
                .build();

        return messageMapper.toDto(messageRepository.save(message));
    }

    /**
     * Отправляет файл в чат заказа.
     *
     * @param orderId ID заказа
     * @param senderId ID отправителя
     * @param attachmentUrl URL файла
     * @return MessageDto отправленного файла
     * @throws IllegalArgumentException если заказ или отправитель не найдены
     * @throws SecurityException если отправитель не участвует в заказе
     */
    @Override
    @Transactional
    public MessageDto sendAttachment(Long orderId, Long senderId, String attachmentUrl) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Заказ не найден -  " + orderId));
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден - " + senderId));

        Message message = Message.builder()
                .order(order)
                .sender(sender)
                .attachmentUrl(attachmentUrl)
                .isPreview(false)
                .sentAt(LocalDateTime.now())
                .build();

        return messageMapper.toDto(messageRepository.save(message));
    }

    /**
     * Получает сообщения по заказу с пагинацией.
     *
     * @param orderId ID заказа
     * @param pageable параметры пагинации
     * @return страница с сообщениями заказа
     * @throws IllegalArgumentException если заказ не найден
     */
    @Override
    public Page<MessageDto> getMessagesByOrder(Long orderId, Pageable pageable) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Заказ не найден - " + orderId));

        return messageRepository.findByOrder(order, pageable)
                .map(messageMapper::toDto);
    }

    @Override
    public List<MessageDto> getPreviewMessages(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Заказ не найден - " + orderId));

        return messageRepository.findByOrderAndIsPreviewTrue(order).stream()
                .map(messageMapper::toDto)
                .toList();
    }

    @Override
    public List<MessageDto> getMessagesWithAttachments(Long orderId) {
        return messageRepository.findMessagesWithAttachments(orderId).stream()
                .map(messageMapper::toDto)
                .toList();
    }

    /**
     * Проверяет, может ли пользователь получить доступ к сообщениям заказа.
     *
     * @param orderId ID заказа
     * @param userId ID пользователя
     * @return true если пользователь заказчик или художник по этому заказу
     */
    @Override
    public boolean canUserAccessOrderMessages(Long orderId, Long userId) {
        Order order = orderRepository.findById(orderId).orElse(null);
        if (order == null) {
            return false;
        }

        return order.getCustomer().getId().equals(userId) ||
                order.getArtist().getId().equals(userId);
    }

    /**
     * Возвращает количество сообщений-предпросмотров по заказу.
     *
     * @param orderId ID заказа
     * @return количество предпросмотров
     */
    @Override
    public long getPreviewMessagesCount(Long orderId) {
        return messageRepository.countPreviewMessages(orderId);
    }


    /**
     * Внутренний метод для отправки сообщения.
     * Универсальная логика для заказов и студий.
     *
     * @param orderId ID заказа (может быть null)
     * @param senderId ID отправителя
     * @param content текст сообщения
     * @param attachmentUrl URL вложения
     * @param isPreview флаг предпросмотра
     * @return MessageDto отправленного сообщения
     */
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
        return messageMapper.toDto(saved);
    }


    /**
     * Возвращает сообщения студии с пагинацией.
     *
     * @param studioId идентификатор студии
     * @param pageable параметры пагинации
     * @return страница с сообщениями студии
     * @throws IllegalArgumentException если студия не найдена
     */
    @Override
    public Page<MessageDto> getMessagesByStudio(Long studioId, Pageable pageable) {
        Studio studio = studioRepository.findById(studioId)
                .orElseThrow(() -> new IllegalArgumentException("Студия не найдена - " + studioId));
        return messageRepository.findByStudio(studio, pageable)
                .map(messageMapper::toDto);
    }

    /**
     * Отправляет сообщение в чат студии.
     * Сообщение видят все менеджеры студии.
     *
     * @param studioId ID студии
     * @param senderId ID отправителя
     * @param content текст сообщения
     * @param attachmentUrl URL файла (может быть null)
     * @return MessageDto отправленного сообщения
     * @throws IllegalArgumentException если студия или отправитель не найдены
     */
    @Override
    @Transactional
    public MessageDto sendToStudio(Long studioId, Long senderId, String content, String attachmentUrl) {
        Studio studio = studioRepository.findById(studioId)
                .orElseThrow(() -> new IllegalArgumentException("Студия не найдена - " + studioId));
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден - " + senderId));

        Message message = Message.builder()
                .studio(studio)
                .sender(sender)
                .content(content)
                .attachmentUrl(attachmentUrl)
                .isPreview(false)
                .isRedirected(false)
                .sentAt(LocalDateTime.now())
                .build();

        return messageMapper.toDto(messageRepository.save(message));
    }

    /**
     * Отправляет сообщение конкретному художнику от имени менеджера.
     * Используется для перенаправления клиента к конкретному художнику.
     *
     * @param studioId ID студии
     * @param senderId ID отправителя (менеджера)
     * @param receiverId ID получателя (художника)
     * @param content текст сообщения
     * @param attachmentUrl URL файла (может быть null)
     * @return MessageDto отправленного сообщения
     * @throws IllegalArgumentException если студия, отправитель или получатель не найдены
     * @throws SecurityException если отправитель не является менеджером студии
     */
    @Override
    @Transactional
    public MessageDto sendToArtist(Long studioId, Long senderId, Long receiverId, String content, String attachmentUrl) {
        Studio studio = studioRepository.findById(studioId)
                .orElseThrow(() -> new IllegalArgumentException("Студия не найдена - " + studioId));
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден - " + senderId));
        User receiver = userRepository.findById(receiverId)
                .orElseThrow(() -> new IllegalArgumentException("Получатель не найден - " + receiverId));

        Message message = Message.builder()
                .studio(studio)
                .sender(sender)
                .receiver(receiver)
                .content(content)
                .attachmentUrl(attachmentUrl)
                .isPreview(false)
                .isRedirected(true)
                .sentAt(LocalDateTime.now())
                .build();

        return messageMapper.toDto(messageRepository.save(message));
    }

    /**
     * Перенаправляет существующее сообщение конкретному художнику.
     *
     * @param messageId ID исходного сообщения
     * @param receiverId ID получателя (художника)
     * @param managerId ID менеджера
     * @return MessageDto перенаправленного сообщения
     * @throws IllegalArgumentException если сообщение или получатель не найдены
     * @throws SecurityException если менеджер не имеет прав
     */
    @Override
    @Transactional
    public MessageDto redirectToArtist(Long messageId, Long receiverId, Long managerId) {
        Message original = messageRepository.findById(messageId)
                .orElseThrow(() -> new IllegalArgumentException("Сообщение не найдено - " + messageId));

        User receiver = userRepository.findById(receiverId)
                .orElseThrow(() -> new IllegalArgumentException("Получатель не найден - " + receiverId));
        User manager = userRepository.findById(managerId)
                .orElseThrow(() -> new IllegalArgumentException("Менеджер не найден - " + managerId));

        Message redirected = Message.builder()
                .studio(original.getStudio())
                .sender(manager)
                .receiver(receiver)
                .content(original.getContent())
                .attachmentUrl(original.getAttachmentUrl())
                .isPreview(original.getIsPreview())
                .isRedirected(true)
                .sentAt(LocalDateTime.now())
                .build();

        return messageMapper.toDto(messageRepository.save(redirected));
    }

    /**
     * Возвращает список сообщений, не перенаправленных конкретному художнику.
     *
     * @param studioId ID студии
     * @return список сообщений без назначенного получателя
     */
    @Override
    public List<MessageDto> getUnassignedStudioMessages(Long studioId) {
        Studio studio = studioRepository.findById(studioId)
                .orElseThrow(() -> new IllegalArgumentException("Студия не найдена - " + studioId));
        return messageRepository.findByStudioAndReceiverIsNull(studio).stream()
                .map(messageMapper::toDto)
                .toList();
    }

}

