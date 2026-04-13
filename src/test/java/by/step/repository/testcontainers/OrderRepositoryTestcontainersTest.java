package by.step.repository.testcontainers;

import by.step.DataServiceApplication;
import by.step.entity.Order;
import by.step.entity.User;
import by.step.entity.enums.OrderStatus;
import by.step.repository.OrderRepository;
import by.step.repository.UserRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@SpringBootTest(classes = DataServiceApplication.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
class OrderRepositoryTestcontainersTest extends PostgresTestcontainersBase {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void findById() {
        Order order = orderRepository.findById(1L).orElseThrow();
        Assertions.assertThat(order).isNotNull();
        Assertions.assertThat(order.getDescription()).isEqualTo("Скин гнома пивовара");
    }

    @Test
    void findByCustomer() {
        User customer = userRepository.findById(1L).orElseThrow();
        Pageable pageable = PageRequest.of(0, 10);
        Page<Order> orders = orderRepository.findByCustomer(customer, pageable);

        Assertions.assertThat(orders.getContent()).isNotEmpty();
        Assertions.assertThat(orders.getContent())
                .extracting(Order::getCustomer)
                .extracting(User::getId)
                .containsOnly(1L);
    }

    @Test
    void findByArtist() {
        User artist = userRepository.findById(2L).orElseThrow();
        Pageable pageable = PageRequest.of(0, 10);
        Page<Order> orders = orderRepository.findByArtist(artist, pageable);

        Assertions.assertThat(orders.getContent()).isNotEmpty();
        Assertions.assertThat(orders.getContent())
                .extracting(Order::getArtist)
                .extracting(User::getId)
                .containsOnly(2L);
    }

    @Test
    void findByStatus() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Order> orders = orderRepository.findByStatus(OrderStatus.COMPLETED, pageable);

        Assertions.assertThat(orders.getContent()).isNotEmpty();
        Assertions.assertThat(orders.getContent())
                .extracting(Order::getStatus)
                .containsOnly(OrderStatus.COMPLETED);
    }

    @Test
    void findByArtistAndStatusIn() {
        User artist = userRepository.findById(2L).orElseThrow();
        List<OrderStatus> statuses = List.of(OrderStatus.NEW, OrderStatus.IN_PROGRESS, OrderStatus.REVIEW);
        List<Order> orders = orderRepository.findByArtistAndStatusIn(artist, statuses);

        Assertions.assertThat(orders).isNotEmpty();
        Assertions.assertThat(orders)
                .extracting(Order::getStatus)
                .allMatch(statuses::contains);
    }

    @Test
    void findByCustomerAndStatus() {
        List<Order> orders = orderRepository.findByCustomerAndStatus(1L, OrderStatus.COMPLETED);

        Assertions.assertThat(orders).isNotEmpty();
        Assertions.assertThat(orders)
                .extracting(Order::getCustomer)
                .extracting(User::getId)
                .containsOnly(1L);
        Assertions.assertThat(orders)
                .extracting(Order::getStatus)
                .containsOnly(OrderStatus.COMPLETED);
    }

    @Test
    void findByCreatedAtBetween() {
        java.time.LocalDateTime start = java.time.LocalDateTime.of(2024, 1, 1, 0, 0);
        java.time.LocalDateTime end = java.time.LocalDateTime.of(2024, 2, 1, 0, 0);
        List<Order> orders = orderRepository.findByCreatedAtBetween(start, end);

        Assertions.assertThat(orders).isNotEmpty();
    }

    @Test
    void checkGetTotalEarnings() {
        BigDecimal earnings = orderRepository.getTotalEarnings(2L);
        Assertions.assertThat(earnings).isNotNull();
        Assertions.assertThat(earnings).isEqualByComparingTo(BigDecimal.valueOf(1500));
    }

    @Test
    void checkGetCompletedOrdersCount() {
        long count = orderRepository.getCompletedOrdersCount(2L);
        Assertions.assertThat(count).isEqualTo(1);
    }

    @Test
    @Transactional
    void checkUpdateStatus() {
        orderRepository.updateStatus(2L, OrderStatus.COMPLETED);
        Order order = orderRepository.findById(2L).orElseThrow();
        Assertions.assertThat(order.getStatus()).isEqualTo(OrderStatus.COMPLETED);
    }

    @Test
    @Transactional
    void checkUpdateFinalFile() {
        String fileUrl = "/uploads/orders/2/final.png";
        orderRepository.updateFinalFile(2L, fileUrl);
        Order order = orderRepository.findById(2L).orElseThrow();
        Assertions.assertThat(order.getFinalFileUrl()).isEqualTo(fileUrl);
    }
}