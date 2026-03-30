package by.step.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageDto {
    private Long id;
    private Long orderId;
    private Long senderId;
    private String senderName;
    private String content;
    private String attachmentUrl;
    private Boolean isPreview;
    private LocalDateTime sentAt;
}
