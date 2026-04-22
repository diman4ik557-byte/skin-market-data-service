package by.step.service;

import by.step.dto.OrderDto;
import by.step.entity.Order;
import by.step.entity.User;
import by.step.enums.OrderStatus;
import by.step.enums.UserRole;
import by.step.repository.OrderRepository;
import by.step.repository.UserRepository;
import by.step.service.impl.OrderServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Тесты сервиса заказов")
class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private OrderServiceImpl orderService;

    private User customer;
    private User artist;
    private Order testOrder;

    @BeforeEach
    void setUp() {
        customer = User.builder()
                .id(1L)
                .username("customer")
                .email("customer@example.com")
                .role(UserRole.USER)
                .balance(BigDecimal.valueOf(5000))
                .build();

        artist = User.builder()
                .id(2L)
                .username("artist")
                .email("artist@example.com")
                .role(UserRole.ARTIST)
                .balance(BigDecimal.valueOf(1000))
                .build();

        testOrder = Order.builder()
                .id(1L)
                .customer(customer)
                .artist(artist)
                .status(OrderStatus.NEW)
                .description("Test order description")
                .price(BigDecimal.valueOf(1000))
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Создание заказа - успех")
    void createOrder_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(userRepository.findById(2L)).thenReturn(Optional.of(artist));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        OrderDto result = orderService.createOrder(1L, 2L, "Test order", BigDecimal.valueOf(1000));

        assertThat(result).isNotNull();
        assertThat(result.getCustomerId()).isEqualTo(1L);
        assertThat(result.getArtistId()).isEqualTo(2L);
        assertThat(result.getStatus()).isEqualTo(OrderStatus.NEW);
        assertThat(result.getPrice()).isEqualByComparingTo(BigDecimal.valueOf(1000));

        verify(orderRepository, times(1)).save(any(Order.class));
        verify(userRepository, times(1)).save(customer);
    }

    @Test
    @DisplayName("Создание заказа - заказчик не найден")
    void createOrder_CustomerNotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.createOrder(999L, 2L, "Test", BigDecimal.valueOf(1000)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Заказчик не найден");

        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("Создание заказа - художник не найден")
    void createOrder_ArtistNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.createOrder(1L, 999L,
                "Test", BigDecimal.valueOf(1000)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Художник не найден");

        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("Создание заказа - пользователь не художник")
    void createOrder_UserNotArtist() {
        customer.setRole(UserRole.USER); // customer не художник
        when(userRepository.findById(1L)).thenReturn(Optional.of(customer));

        assertThatThrownBy(() -> orderService.createOrder(1L, 1L,
                "Test", BigDecimal.valueOf(1000)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("не является художником");
    }

    @Test
    @DisplayName("Создание заказа - недостаточно средств")
    void createOrder_InsufficientFunds() {
        customer.setBalance(BigDecimal.valueOf(500));
        when(userRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(userRepository.findById(2L)).thenReturn(Optional.of(artist));

        assertThatThrownBy(() -> orderService.createOrder(1L, 2L,
                "Test", BigDecimal.valueOf(1000)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Недостаточно средств");
    }

    @Test
    @DisplayName("Поиск заказа по ID - успех")
    void findById_Success() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

        OrderDto result = orderService.findById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("Поиск заказа по ID - не найден")
    void findById_NotFound() {
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.findById(999L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Заказ не найден");
    }

    @Test
    @DisplayName("Начало выполнения заказа - успех")
    void startOrder_Success() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        orderService.startOrder(1L);

        verify(orderRepository, times(1)).save(testOrder);
        assertThat(testOrder.getStatus()).isEqualTo(OrderStatus.IN_PROGRESS);
    }

    @Test
    @DisplayName("Начало выполнения заказа - заказ не в статусе NEW")
    void startOrder_NotNewStatus() {
        testOrder.setStatus(OrderStatus.IN_PROGRESS);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

        assertThatThrownBy(() -> orderService.startOrder(1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("должен быть в статусе NEW");
    }

    @Test
    @DisplayName("Отправка на проверку - успех")
    void submitForReview_Success() {
        testOrder.setStatus(OrderStatus.IN_PROGRESS);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        orderService.submitForReview(1L, "file-url.png");

        assertThat(testOrder.getStatus()).isEqualTo(OrderStatus.REVIEW);
        assertThat(testOrder.getFinalFileUrl()).isEqualTo("file-url.png");
    }

    @Test
    @DisplayName("Завершение заказа - успех")
    void completeOrder_Success() {
        testOrder.setStatus(OrderStatus.REVIEW);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);
        when(userRepository.save(any(User.class))).thenReturn(artist);

        orderService.completeOrder(1L);

        assertThat(testOrder.getStatus()).isEqualTo(OrderStatus.COMPLETED);
        assertThat(testOrder.getCompletedAt()).isNotNull();
        verify(userRepository, times(1)).save(artist);
    }

    @Test
    @DisplayName("Отмена заказа - успех")
    void cancelOrder_Success() {
        testOrder.setStatus(OrderStatus.NEW);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);
        when(userRepository.save(any(User.class))).thenReturn(customer);

        orderService.cancelOrder(1L);

        assertThat(testOrder.getStatus()).isEqualTo(OrderStatus.CANCELLED);
    }

    @Test
    @DisplayName("Отмена заказа - нельзя отменить завершенный")
    void cancelOrder_CompletedOrder() {
        testOrder.setStatus(OrderStatus.COMPLETED);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

        assertThatThrownBy(() -> orderService.cancelOrder(1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Нельзя отменить завершенный заказ");
    }

    @Test
    @DisplayName("Получение заработка художника")
    void getTotalEarnings_Success() {
        when(orderRepository.getTotalEarnings(2L)).thenReturn(BigDecimal.valueOf(1500));

        BigDecimal earnings = orderService.getTotalEarnings(2L);

        assertThat(earnings).isEqualByComparingTo(BigDecimal.valueOf(1500));
    }

    // дополнительно

    @Test
    @DisplayName("Поиск активных заказов художника ")
    void findActiveOrdersByArtist_Success() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(artist));
        when(orderRepository.findByArtistAndStatusIn(eq(artist), anyList()))
                .thenReturn(List.of(testOrder));

        List<OrderDto> result = orderService.findActiveOrdersByArtist(2L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(1L);
        verify(orderRepository, times(1)).findByArtistAndStatusIn(eq(artist), anyList());
    }

    @Test
    @DisplayName("Поиск активных заказов художника - художник не найден")
    void findActiveOrdersByArtist_ArtistNotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.findActiveOrdersByArtist(999L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Художник не найден");
    }

    @Test
    @DisplayName("Поиск заказов заказчика как список")
    void findByCustomerAsList_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(orderRepository.findByCustomer(eq(customer), any(Pageable.class)))
                .thenReturn(new org.springframework.data.domain.PageImpl<>(List.of(testOrder)));

        List<OrderDto> result = orderService.findByCustomerAsList(1L);

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("Поиск заказов заказчика как список - заказчик не найден")
    void findByCustomerAsList_CustomerNotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.findByCustomerAsList(999L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Заказчик не найден");
    }

    @Test
    @DisplayName("Поиск заказов заказчика с пагинацией")
    void findByCustomer_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        when(userRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(orderRepository.findByCustomer(eq(customer), eq(pageable)))
                .thenReturn(new org.springframework.data.domain.PageImpl<>(List.of(testOrder)));

        Page<OrderDto> result = orderService.findByCustomer(1L, pageable);

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    @DisplayName("Поиск заказов художника с пагинацией - успех")
    void findByArtist_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        when(userRepository.findById(2L)).thenReturn(Optional.of(artist));
        when(orderRepository.findByArtist(eq(artist), eq(pageable)))
                .thenReturn(new org.springframework.data.domain.PageImpl<>(List.of(testOrder)));

        Page<OrderDto> result = orderService.findByArtist(2L, pageable);

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    @DisplayName("Обновление статуса заказа - успех")
    void updateStatus_Success() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        orderService.updateStatus(1L, OrderStatus.IN_PROGRESS);

        assertThat(testOrder.getStatus()).isEqualTo(OrderStatus.IN_PROGRESS);
        verify(orderRepository, times(1)).save(testOrder);
    }

    @Test
    @DisplayName("Обновление статуса заказа - заказ не найден")
    void updateStatus_OrderNotFound() {
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.updateStatus(999L, OrderStatus.IN_PROGRESS))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Заказ не найден");
    }

    @Test
    @DisplayName("Обновление финального файла")
    void updateFinalFile_Success() {
        doNothing().when(orderRepository).updateFinalFile(1L, "file.png");

        orderService.updateFinalFile(1L, "file.png");

        verify(orderRepository, times(1)).updateFinalFile(1L, "file.png");
    }

    @Test
    @DisplayName("Поиск заказов по статусу с пагинацией")
    void findByStatus_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        when(orderRepository.findByStatus(eq(OrderStatus.NEW), eq(pageable)))
                .thenReturn(new org.springframework.data.domain.PageImpl<>(List.of(testOrder)));

        Page<OrderDto> result = orderService.findByStatus(OrderStatus.NEW, pageable);

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    @DisplayName("Получение количества завершённых заказов художника")
    void getCompletedOrdersCount_Success() {
        when(orderRepository.getCompletedOrdersCount(2L)).thenReturn(5L);

        long result = orderService.getCompletedOrdersCount(2L);

        assertThat(result).isEqualTo(5L);
    }

    @Test
    @DisplayName("Завершение заказа - заказ не в статусе REVIEW")
    void completeOrder_NotInReviewStatus() {
        testOrder.setStatus(OrderStatus.NEW);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

        assertThatThrownBy(() -> orderService.completeOrder(1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("должен быть в статусе REVIEW");
    }

    @Test
    @DisplayName("Отправка на проверку - заказ не в статусе IN_PROGRESS")
    void submitForReview_NotInProgressStatus() {
        testOrder.setStatus(OrderStatus.NEW);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

        assertThatThrownBy(() -> orderService.submitForReview(1L,
                "file.png"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("должен быть в статусе IN_PROGRESS");
    }
}