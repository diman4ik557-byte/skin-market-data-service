package by.step.service;

import by.step.dto.OrderDto;
import by.step.entity.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

public interface OrderService {

    OrderDto createOrder(Long customerId, Long artistId, String description, BigDecimal price);

    OrderDto findById(Long orderId);

    Page<OrderDto> findByCustomer(Long customerId, Pageable pageable);

    Page<OrderDto> findByArtist(Long artistId, Pageable pageable);

    Page<OrderDto> findByStatus(OrderStatus status, Pageable pageable);

    List<OrderDto> findActiveOrdersByArtist(Long artistId);

    void updateStatus(Long orderId, OrderStatus status);

    void updateFinalFile(Long orderId, String fileUrl);

    void startOrder(Long orderId);

    void submitForReview(Long orderId, String finalFileUrl);

    void completeOrder(Long orderId);

    void cancelOrder(Long orderId);

    BigDecimal getTotalEarnings(Long artistId);

    long getCompletedOrdersCount(Long artistId);
}
