package by.step.controller;

import by.step.dto.MessageDto;
import by.step.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    @PostMapping("/order/{orderId}/send")
    public ResponseEntity<MessageDto> sendMessage(
            @PathVariable Long orderId,
            @RequestParam Long senderId,
            @RequestParam String content) {
        MessageDto message = messageService.sendMessage(orderId, senderId, content);
        return ResponseEntity.ok(message);
    }

    @PostMapping("/order/{orderId}/preview")
    public ResponseEntity<MessageDto> sendPreview(
            @PathVariable Long orderId,
            @RequestParam Long senderId,
            @RequestParam String content,
            @RequestParam String attachmentUrl) {
        MessageDto message = messageService.sendPreview(orderId, senderId, content, attachmentUrl);
        return ResponseEntity.ok(message);
    }

    @PostMapping("/order/{orderId}/attachment")
    public ResponseEntity<MessageDto> sendAttachment(
            @PathVariable Long orderId,
            @RequestParam Long senderId,
            @RequestParam String attachmentUrl) {
        MessageDto message = messageService.sendAttachment(orderId, senderId, attachmentUrl);
        return ResponseEntity.ok(message);
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<Page<MessageDto>> getMessagesByOrder(
            @PathVariable Long orderId,
            Pageable pageable) {
        return ResponseEntity.ok(messageService.getMessagesByOrder(orderId, pageable));
    }

    @GetMapping("/order/{orderId}/previews")
    public ResponseEntity<List<MessageDto>> getPreviewMessages(@PathVariable Long orderId) {
        return ResponseEntity.ok(messageService.getPreviewMessages(orderId));
    }

    @GetMapping("/order/{orderId}/attachments")
    public ResponseEntity<List<MessageDto>> getMessagesWithAttachments(@PathVariable Long orderId) {
        return ResponseEntity.ok(messageService.getMessagesWithAttachments(orderId));
    }

    @GetMapping("/order/{orderId}/access")
    public ResponseEntity<Boolean> canAccess(
            @PathVariable Long orderId,
            @RequestParam Long userId) {
        return ResponseEntity.ok(messageService.canUserAccessOrderMessages(orderId, userId));
    }

    @GetMapping("/order/{orderId}/previews/count")
    public ResponseEntity<Long> getPreviewMessagesCount(@PathVariable Long orderId) {
        return ResponseEntity.ok(messageService.getPreviewMessagesCount(orderId));
    }
}