package by.step.controller;

import by.step.dto.MessageDto;
import by.step.service.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST контроллер для работы с сообщениями.
 * Предоставляет API для отправки и получения сообщений в заказах и студиях.
 *
 * @author Skin Market Team
 * @version 1.0
 */
@Slf4j
@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    /**
     * Получает сообщения по заказу с пагинацией.
     *
     * @param orderId ID заказа
     * @param pageable параметры пагинации (page, size, sort)
     * @return страница с сообщениями заказа
     */
    @GetMapping("/order/{orderId}")
    public ResponseEntity<Page<MessageDto>> getMessagesByOrder(
            @PathVariable Long orderId,
            Pageable pageable) {
        log.debug("REST запрос: получение сообщений по заказу {}", orderId);
        return ResponseEntity.ok(messageService.getMessagesByOrder(orderId, pageable));
    }

    /**
     * Отправляет обычное сообщение в чат заказа.
     *
     * @param orderId ID заказа
     * @param senderId ID отправителя
     * @param content текст сообщения
     * @return созданное сообщение
     */
    @PostMapping("/order/{orderId}/send")
    public ResponseEntity<MessageDto> sendMessage(
            @PathVariable Long orderId,
            @RequestParam Long senderId,
            @RequestParam String content) {
        log.info("REST запрос: отправка сообщения в заказ {} от пользователя {}", orderId, senderId);
        return ResponseEntity.ok(messageService.sendMessage(orderId, senderId, content));
    }

    /**
     * Отправляет предпросмотр работы в чат заказа.
     *
     * @param orderId ID заказа
     * @param senderId ID отправителя
     * @param content описание предпросмотра
     * @param attachmentUrl URL изображения предпросмотра
     * @return созданное сообщение с предпросмотром
     */
    @PostMapping("/order/{orderId}/preview")
    public ResponseEntity<MessageDto> sendPreview(
            @PathVariable Long orderId,
            @RequestParam Long senderId,
            @RequestParam String content,
            @RequestParam String attachmentUrl) {
        log.info("REST запрос: отправка предпросмотра в заказ {} от пользователя {}", orderId, senderId);
        return ResponseEntity.ok(messageService.sendPreview(orderId, senderId, content, attachmentUrl));
    }

    /**
     * Отправляет файл в чат заказа.
     *
     * @param orderId ID заказа
     * @param senderId ID отправителя
     * @param attachmentUrl URL файла
     * @return созданное сообщение с файлом
     */
    @PostMapping("/order/{orderId}/attachment")
    public ResponseEntity<MessageDto> sendAttachment(
            @PathVariable Long orderId,
            @RequestParam Long senderId,
            @RequestParam String attachmentUrl) {
        log.info("REST запрос: отправка файла в заказ {} от пользователя {}", orderId, senderId);
        return ResponseEntity.ok(messageService.sendAttachment(orderId, senderId, attachmentUrl));
    }

    /**
     * Проверяет, может ли пользователь получить доступ к сообщениям заказа.
     *
     * @param orderId ID заказа
     * @param userId ID пользователя
     * @return true если пользователь заказчик или художник по этому заказу
     */
    @GetMapping("/order/{orderId}/access")
    public ResponseEntity<Boolean> canAccess(
            @PathVariable Long orderId,
            @RequestParam Long userId) {
        log.debug("REST запрос: проверка доступа пользователя {} к заказу {}", userId, orderId);
        return ResponseEntity.ok(messageService.canUserAccessOrderMessages(orderId, userId));
    }

    /**
     * Возвращает количество сообщений-предпросмотров по заказу.
     *
     * @param orderId ID заказа
     * @return количество предпросмотров
     */
    @GetMapping("/order/{orderId}/previews/count")
    public ResponseEntity<Long> getPreviewMessagesCount(@PathVariable Long orderId) {
        log.debug("REST запрос: получение количества предпросмотров по заказу {}", orderId);
        return ResponseEntity.ok(messageService.getPreviewMessagesCount(orderId));
    }


    /**
     * Получает сообщения по студии с пагинацией.
     *
     * @param studioId ID студии
     * @param pageable параметры пагинации (page, size, sort)
     * @return страница с сообщениями студии
     */
    @GetMapping("/studio/{studioId}")
    public ResponseEntity<Page<MessageDto>> getStudioMessages(
            @PathVariable Long studioId,
            Pageable pageable) {
        log.debug("REST запрос: получение сообщений по студии {}", studioId);
        return ResponseEntity.ok(messageService.getMessagesByStudio(studioId, pageable));
    }

    /**
     * Отправляет сообщение в чат студии.
     *
     * @param studioId ID студии
     * @param senderId ID отправителя
     * @param content текст сообщения
     * @param attachmentUrl URL файла (опционально)
     * @return созданное сообщение
     */
    @PostMapping("/studio/{studioId}/send")
    public ResponseEntity<MessageDto> sendToStudio(
            @PathVariable Long studioId,
            @RequestParam Long senderId,
            @RequestParam String content,
            @RequestParam(required = false) String attachmentUrl) {
        log.info("REST запрос: отправка сообщения в студию {} от пользователя {}", studioId, senderId);
        return ResponseEntity.ok(messageService.sendToStudio(studioId, senderId, content, attachmentUrl));
    }

    /**
     * Отправляет сообщение конкретному художнику от имени менеджера студии.
     *
     * @param studioId ID студии
     * @param senderId ID отправителя (менеджера)
     * @param receiverId ID получателя (художника)
     * @param content текст сообщения
     * @param attachmentUrl URL файла (опционально)
     * @return созданное сообщение
     */
    @PostMapping("/studio/{studioId}/send-to-artist")
    public ResponseEntity<MessageDto> sendToArtist(
            @PathVariable Long studioId,
            @RequestParam Long senderId,
            @RequestParam Long receiverId,
            @RequestParam String content,
            @RequestParam(required = false) String attachmentUrl) {
        log.info("REST запрос: перенаправление сообщения в студии {} от {} к {}", studioId, senderId, receiverId);
        return ResponseEntity.ok(messageService.sendToArtist(studioId, senderId, receiverId, content, attachmentUrl));
    }

    /**
     * Перенаправляет существующее сообщение конкретному художнику.
     *
     * @param messageId ID исходного сообщения
     * @param receiverId ID получателя (художника)
     * @param managerId ID менеджера
     * @return перенаправленное сообщение
     */
    @PostMapping("/{messageId}/redirect")
    public ResponseEntity<MessageDto> redirectToArtist(
            @PathVariable Long messageId,
            @RequestParam Long receiverId,
            @RequestParam Long managerId) {
        log.info("REST запрос: перенаправление сообщения {} пользователю {}", messageId, receiverId);
        return ResponseEntity.ok(messageService.redirectToArtist(messageId, receiverId, managerId));
    }

    /**
     * Возвращает список сообщений студии, не перенаправленных конкретному художнику.
     *
     * @param studioId ID студии
     * @return список неназначенных сообщений
     */
    @GetMapping("/studio/{studioId}/unassigned")
    public ResponseEntity<java.util.List<MessageDto>> getUnassignedStudioMessages(@PathVariable Long studioId) {
        log.debug("REST запрос: получение неназначенных сообщений студии {}", studioId);
        return ResponseEntity.ok(messageService.getUnassignedStudioMessages(studioId));
    }
}