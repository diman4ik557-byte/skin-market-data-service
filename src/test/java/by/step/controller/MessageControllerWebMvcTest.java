package by.step.controller;

import by.step.dto.MessageDto;
import by.step.service.MessageService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MessageController.class)
@ActiveProfiles("test")
class MessageControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MessageService messageService;

    @Autowired
    private ObjectMapper objectMapper;

    private MessageDto testMessageDto;
    private MessageDto previewMessageDto;
    private Page<MessageDto> testMessagePage;

    @BeforeEach
    void setUp() {
        testMessageDto = MessageDto.builder()
                .id(1L)
                .orderId(1L)
                .senderId(1L)
                .senderName("ivan")
                .content("Здравствуйте! Хочу заказать гнома пивовара")
                .isPreview(false)
                .sentAt(LocalDateTime.of(2024, 1, 10, 10, 5))
                .build();

        previewMessageDto = MessageDto.builder()
                .id(4L)
                .orderId(2L)
                .senderId(2L)
                .senderName("petr")
                .content("Вот набросок, посмотрите")
                .attachmentUrl("/uploads/previews/order2_sketch.png")
                .isPreview(true)
                .sentAt(LocalDateTime.of(2024, 3, 2, 11, 0))
                .build();

        testMessagePage = new PageImpl<>(List.of(testMessageDto), PageRequest.of(0, 10), 1);
    }

    @Test
    void checkSendMessage() throws Exception {
        when(messageService.sendMessage(anyLong(), anyLong(), anyString()))
                .thenReturn(testMessageDto);

        mockMvc.perform(post("/api/messages/order/{orderId}/send", 1L)
                        .param("senderId", "1")
                        .param("content", "Здравствуйте! Хочу заказать гнома пивовара"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.senderName").value("ivan"))
                .andExpect(jsonPath("$.content").value("Здравствуйте! Хочу заказать гнома пивовара"));
    }

    @Test
    void checkSendPreview() throws Exception {
        when(messageService.sendPreview(anyLong(), anyLong(), anyString(), anyString()))
                .thenReturn(previewMessageDto);

        mockMvc.perform(post("/api/messages/order/{orderId}/preview", 2L)
                        .param("senderId", "2")
                        .param("content", "Вот набросок, посмотрите")
                        .param("attachmentUrl", "/uploads/previews/order2_sketch.png"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isPreview").value(true))
                .andExpect(jsonPath("$.attachmentUrl").value("/uploads/previews/order2_sketch.png"));
    }

    @Test
    void checkSendAttachment() throws Exception {
        MessageDto attachmentMessage = MessageDto.builder()
                .id(5L)
                .orderId(1L)
                .senderId(1L)
                .senderName("ivan")
                .attachmentUrl("/uploads/attachments/reference.png")
                .isPreview(false)
                .build();

        when(messageService.sendAttachment(anyLong(), anyLong(), anyString()))
                .thenReturn(attachmentMessage);

        mockMvc.perform(post("/api/messages/order/{orderId}/attachment", 1L)
                        .param("senderId", "1")
                        .param("attachmentUrl", "/uploads/attachments/reference.png"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.attachmentUrl").value("/uploads/attachments/reference.png"));
    }

    @Test
    void checkGetMessagesByOrder() throws Exception {
        when(messageService.getMessagesByOrder(eq(1L), any(Pageable.class)))
                .thenReturn(testMessagePage);

        mockMvc.perform(get("/api/messages/order/{orderId}", 1L)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].content").value("Здравствуйте! Хочу заказать гнома пивовара"));
    }

    @Test
    void checkGetPreviewMessages() throws Exception {
        List<MessageDto> previews = List.of(previewMessageDto);
        when(messageService.getPreviewMessages(2L)).thenReturn(previews);

        mockMvc.perform(get("/api/messages/order/{orderId}/previews", 2L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].isPreview").value(true));
    }

    @Test
    void checkGetMessagesWithAttachments() throws Exception {
        List<MessageDto> attachments = List.of(testMessageDto);
        when(messageService.getMessagesWithAttachments(1L)).thenReturn(attachments);

        mockMvc.perform(get("/api/messages/order/{orderId}/attachments", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void checkCanAccess() throws Exception {
        when(messageService.canUserAccessOrderMessages(1L, 1L)).thenReturn(true);

        mockMvc.perform(get("/api/messages/order/{orderId}/access", 1L)
                        .param("userId", "1"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    void checkGetPreviewMessagesCount() throws Exception {
        when(messageService.getPreviewMessagesCount(2L)).thenReturn(1L);

        mockMvc.perform(get("/api/messages/order/{orderId}/previews/count", 2L))
                .andExpect(status().isOk())
                .andExpect(content().string("1"));
    }
}