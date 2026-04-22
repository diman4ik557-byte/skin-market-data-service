package by.step.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO для передачи данных о сообщении в чате.
 * Поддерживает сообщения как в заказах, так и в студиях.
 *
 * @author Skin Market Team
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageDto {

    /**
     * Уникальный идентификатор сообщения.
     */
    private Long id;

    /**
     * ID заказа, к которому относится сообщение (если применимо).
     */
    private Long orderId;

    /**
     * ID студии, к которой относится сообщение (если применимо).
     */
    private Long studioId;

    /**
     * Название студии (для отображения).
     */
    private String studioName;

    /**
     * ID отправителя сообщения.
     */
    private Long senderId;

    /**
     * Имя отправителя.
     */
    private String senderName;

    /**
     * ID получателя (для перенаправленных сообщений).
     */
    private Long receiverId;

    /**
     * Имя получателя.
     */
    private String receiverName;

    /**
     * Текст сообщения.
     */
    private String content;

    /**
     * URL прикрепленного файла (если есть).
     */
    private String attachmentUrl;

    /**
     * Флаг, указывающий, является ли сообщение предпросмотром работы.
     */
    private Boolean isPreview;

    /**
     * Флаг, указывающий, было ли сообщение перенаправлено.
     */
    private Boolean isRedirected;

    /**
     * Дата и время отправки сообщения.
     */
    private LocalDateTime sentAt;
}