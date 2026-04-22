package by.step.controller;

import by.step.dto.OrderDto;
import by.step.enums.OrderStatus;
import by.step.service.OrderService;
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
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
@DisplayName("Web тесты контроллера заказов")
class OrderControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderService orderService;

    @Autowired
    private ObjectMapper objectMapper;

    private OrderDto testOrderDto;
    private Page<OrderDto> testOrderPage;

    @BeforeEach
    void setUp() {
        testOrderDto = OrderDto.builder()
                .id(1L)
                .customerId(1L)
                .customerName("ivan")
                .artistId(2L)
                .artistName("petr")
                .status(OrderStatus.IN_PROGRESS)
                .description("РЎРєРёРЅ РіРЅРѕРјР° РїРёРІРѕРІР°СЂР°")
                .price(BigDecimal.valueOf(1500))
                .createdAt(LocalDateTime.of(2024, 1, 10, 10, 0))
                .build();

        testOrderPage = new PageImpl<>(List.of(testOrderDto), PageRequest.of(0, 10), 1);
    }

    @Test
    @DisplayName("Создание заказа - успех")
    void createOrder_Success() throws Exception {
        when(orderService.createOrder(anyLong(), anyLong(), anyString(), any(BigDecimal.class)))
                .thenReturn(testOrderDto);

        mockMvc.perform(post("/api/orders")
                        .param("customerId", "1")
                        .param("artistId", "2")
                        .param("description", "РЎРєРёРЅ РіРЅРѕРјР° РїРёРІРѕРІР°СЂР°")
                        .param("price", "1500"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.customerName").value("ivan"))
                .andExpect(jsonPath("$.artistName").value("petr"))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
    }

    @Test
    @DisplayName("Получение заказа по ID")
    void findById_Success() throws Exception {
        when(orderService.findById(1L)).thenReturn(testOrderDto);

        mockMvc.perform(get("/api/orders/{orderId}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("РЎРєРёРЅ РіРЅРѕРјР° РїРёРІРѕРІР°СЂР°"))
                .andExpect(jsonPath("$.price").value(1500));
    }

    @Test
    @DisplayName("Получение заказов заказчика")
    void findByCustomer_Success() throws Exception {
        when(orderService.findByCustomer(eq(1L), any(Pageable.class)))
                .thenReturn(testOrderPage);

        mockMvc.perform(get("/api/orders/customer/{customerId}", 1L)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].customerName").value("ivan"));
    }

    @Test
    @DisplayName("Получение заказов художника")
    void findByArtist_Success() throws Exception {
        when(orderService.findByArtist(eq(2L), any(Pageable.class)))
                .thenReturn(testOrderPage);

        mockMvc.perform(get("/api/orders/artist/{artistId}", 2L)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].artistName").value("petr"));
    }

    @Test
    @DisplayName("Получение заказов по статусу")
    void findByStatus_Success() throws Exception {
        Page<OrderDto> statusPage = new PageImpl<>(List.of(testOrderDto), PageRequest.of(0, 10), 1);
        when(orderService.findByStatus(eq(OrderStatus.IN_PROGRESS), any(Pageable.class)))
                .thenReturn(statusPage);

        mockMvc.perform(get("/api/orders/status/{status}", "IN_PROGRESS")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].status").value("IN_PROGRESS"));
    }


    @Test
    @DisplayName("Обновление статуса заказа")
    void updateStatus_Success() throws Exception {
        doNothing().when(orderService).updateStatus(1L, OrderStatus.COMPLETED);

        mockMvc.perform(put("/api/orders/{orderId}/status", 1L)
                        .param("status", "COMPLETED"))
                .andExpect(status().isOk());

        verify(orderService, times(1)).updateStatus(1L, OrderStatus.COMPLETED);
    }


    @Test
    @DisplayName("Начало выполнения заказа")
    void startOrder_Success() throws Exception {
        doNothing().when(orderService).startOrder(1L);

        mockMvc.perform(post("/api/orders/{orderId}/start", 1L))
                .andExpect(status().isOk());

        verify(orderService, times(1)).startOrder(1L);
    }

    @Test
    @DisplayName("Отправка на проверку")
    void submitForReview_Success() throws Exception {
        doNothing().when(orderService).submitForReview(1L, "/uploads/orders/1/final.png");

        mockMvc.perform(post("/api/orders/{orderId}/submit-review", 1L)
                        .param("finalFileUrl", "/uploads/orders/1/final.png"))
                .andExpect(status().isOk());

        verify(orderService, times(1)).submitForReview(1L, "/uploads/orders/1/final.png");
    }

    @Test
    @DisplayName("Завершение заказа")
    void completeOrder_Success() throws Exception {
        doNothing().when(orderService).completeOrder(1L);

        mockMvc.perform(post("/api/orders/{orderId}/complete", 1L))
                .andExpect(status().isOk());

        verify(orderService, times(1)).completeOrder(1L);
    }

    @Test
    @DisplayName("Отмена заказа")
    void cancelOrder_Success() throws Exception {
        doNothing().when(orderService).cancelOrder(1L);

        mockMvc.perform(post("/api/orders/{orderId}/cancel", 1L))
                .andExpect(status().isOk());

        verify(orderService, times(1)).cancelOrder(1L);
    }

    @Test
    @DisplayName("Получение общего заработка художника")
    void getTotalEarnings_Success() throws Exception {
        when(orderService.getTotalEarnings(2L)).thenReturn(BigDecimal.valueOf(1500));

        mockMvc.perform(get("/api/orders/artist/{artistId}/earnings", 2L))
                .andExpect(status().isOk())
                .andExpect(content().string("1500"));
    }

}