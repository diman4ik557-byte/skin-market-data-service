package by.step.controller;

import by.step.dto.MessageDto;
import by.step.service.MessageService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MessageController.class)
@DisplayName("Web тесты контроллера сообщений")
class MessageControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MessageService messageService;

    @Autowired
    private ObjectMapper objectMapper;

    private MessageDto testMessageDto;
    private Page<MessageDto> testMessagePage;

    @BeforeEach
    void setUp() {
        testMessageDto = MessageDto.builder()
                .id(1L)
                .orderId(1L)
                .senderId(1L)
                .senderName("customer")
                .content("Test message")
                .isPreview(false)
                .sentAt(LocalDateTime.now())
                .build();

        testMessagePage = new PageImpl<>(List.of(testMessageDto),
                PageRequest.of(0, 10), 1);
    }

    @Test
    @DisplayName("Получение сообщений по заказу")
    void getMessagesByOrder_Success() throws Exception {
        when(messageService.getMessagesByOrder(eq(1L), any(Pageable.class)))
                .thenReturn(testMessagePage);

        mockMvc.perform(get("/api/messages/order/{orderId}", 1L)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].content").value("Test message"));
    }

    @Test
    @DisplayName("Отправка сообщения в заказ")
    void sendMessage_Success() throws Exception {
        when(messageService.sendMessage(anyLong(), anyLong(), anyString()))
                .thenReturn(testMessageDto);

        mockMvc.perform(post("/api/messages/order/{orderId}/send", 1L)
                        .param("senderId", "1")
                        .param("content", "Test message"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.content").value("Test message"));
    }

    @Test
    @DisplayName("Отправка предпросмотра")
    void sendPreview_Success() throws Exception {
        MessageDto previewDto = MessageDto.builder()
                .id(2L)
                .orderId(1L)
                .senderId(2L)
                .senderName("artist")
                .content("Preview")
                .attachmentUrl("/uploads/preview.png")
                .isPreview(true)
                .build();

        when(messageService.sendPreview(anyLong(), anyLong(), anyString(), anyString()))
                .thenReturn(previewDto);

        mockMvc.perform(post("/api/messages/order/{orderId}/preview", 1L)
                        .param("senderId", "2")
                        .param("content", "Preview")
                        .param("attachmentUrl", "/uploads/preview.png"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isPreview").value(true))
                .andExpect(jsonPath("$.attachmentUrl")
                        .value("/uploads/preview.png"));
    }

    @Test
    @DisplayName("Отправка файла в заказ")
    void sendAttachment_Success() throws Exception {
        MessageDto attachmentDto = MessageDto.builder()
                .id(3L)
                .orderId(1L)
                .senderId(1L)
                .attachmentUrl("/uploads/file.png")
                .build();

        when(messageService.sendAttachment(anyLong(), anyLong(), anyString()))
                .thenReturn(attachmentDto);

        mockMvc.perform(post("/api/messages/order/{orderId}/attachment", 1L)
                        .param("senderId", "1")
                        .param("attachmentUrl", "/uploads/file.png"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.attachmentUrl")
                        .value("/uploads/file.png"));
    }

    @Test
    @DisplayName("Получение сообщений по студии")
    void getStudioMessages_Success() throws Exception {
        when(messageService.getMessagesByStudio(eq(1L), any(Pageable.class)))
                .thenReturn(testMessagePage);

        mockMvc.perform(get("/api/messages/studio/{studioId}", 1L)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()")
                        .value(1));
    }

    @Test
    @DisplayName("Отправка сообщения в студию")
    void sendToStudio_Success() throws Exception {
        when(messageService.sendToStudio(anyLong(), anyLong(), anyString(), any()))
                .thenReturn(testMessageDto);

        mockMvc.perform(post("/api/messages/studio/{studioId}/send", 1L)
                        .param("senderId", "1")
                        .param("content", "Hello studio"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content")
                        .value("Test message"));
    }

    @Test
    @DisplayName("Проверка доступа к сообщениям заказа")
    void canAccess_Success() throws Exception {
        when(messageService.canUserAccessOrderMessages(1L, 1L)).thenReturn(true);

        mockMvc.perform(get("/api/messages/order/{orderId}/access", 1L)
                        .param("userId", "1"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    @DisplayName("Получение количества предпросмотров")
    void getPreviewMessagesCount_Success() throws Exception {
        when(messageService.getPreviewMessagesCount(1L)).thenReturn(5L);

        mockMvc.perform(get("/api/messages/order/{orderId}/previews/count", 1L))
                .andExpect(status().isOk())
                .andExpect(content().string("5"));
    }
}