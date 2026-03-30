package by.step.service.impl;

import by.step.dto.OrderDto;
import by.step.entity.Order;
import by.step.entity.User;
import by.step.entity.enums.OrderStatus;
import by.step.entity.enums.UserRole;
import by.step.repository.OrderRepository;
import by.step.repository.UserRepository;
import by.step.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    @Override
    public OrderDto createOrder(Long customerId, Long artistId, String description, BigDecimal price) {
        User customer = userRepository.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Заказчик не найден - " + customerId));

        User artist = userRepository.findById(artistId)
                .orElseThrow(() -> new IllegalArgumentException("Художник не найден - " + artistId));

        if (artist.getRole() != UserRole.ARTIST && artist.getRole() != UserRole.ADMIN) {
            throw new IllegalArgumentException("Пользователь не художник - " + artistId);
        }

        if (customer.getBalance().compareTo(price) < 0) {
            throw new IllegalArgumentException("Недостаточно средств");
        }

        Order order = Order.builder()
                .customer(customer)
                .artist(artist)
                .status(OrderStatus.NEW)
                .description(description)
                .price(price)
                .createdAt(LocalDateTime.now())
                .build();

        customer.setBalance(customer.getBalance().subtract(price));
        userRepository.save(customer);

        Order saved = orderRepository.save(order);
        return mapToDto(saved);
    }

    @Override
    public OrderDto findById(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Заказ не найден - " + orderId));
        return mapToDto(order);
    }

    @Override
    public Page<OrderDto> findByCustomer(Long customerId, Pageable pageable) {
        User customer = userRepository.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Заказчик не найден - " + customerId));

        return orderRepository.findByCustomer(customer, pageable)
                .map(this::mapToDto);
    }

    @Override
    public Page<OrderDto> findByArtist(Long artistId, Pageable pageable) {
        User artist = userRepository.findById(artistId)
                .orElseThrow(() -> new IllegalArgumentException("Художник не найден - " + artistId));

        return orderRepository.findByArtist(artist, pageable)
                .map(this::mapToDto);
    }

    @Override
    public Page<OrderDto> findByStatus(OrderStatus status, Pageable pageable) {
        return orderRepository.findByStatus(status, pageable)
                .map(this::mapToDto);
    }

    @Override
    public List<OrderDto> findActiveOrdersByArtist(Long artistId) {
        User artist = userRepository.findById(artistId)
                .orElseThrow(() -> new IllegalArgumentException("Художник не найден - " + artistId));

        List<Order> orders = orderRepository.findByArtistAndStatusIn(
                artist,
                List.of(OrderStatus.NEW, OrderStatus.IN_PROGRESS, OrderStatus.REVIEW)
        );

        return orders.stream()
                .map(this::mapToDto)
                .toList();
    }

    @Override
    @Transactional
    public void updateStatus(Long orderId, OrderStatus status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Художник не найден - " + orderId));

        order.setStatus(status);
        orderRepository.save(order);
    }

    @Override
    @Transactional
    public void updateFinalFile(Long orderId, String fileUrl) {
        orderRepository.updateFinalFile(orderId, fileUrl);
    }

    @Override
    @Transactional
    public void startOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Заказ не найден - " + orderId));

        if (order.getStatus() != OrderStatus.NEW) {
            throw new IllegalStateException("Заказ должен быть NEW");
        }

        order.setStatus(OrderStatus.IN_PROGRESS);
        orderRepository.save(order);
    }

    @Override
    @Transactional
    public void submitForReview(Long orderId, String finalFileUrl) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Заказ не найден " + orderId));

        if (order.getStatus() != OrderStatus.IN_PROGRESS) {
            throw new IllegalStateException("Заказ должен быть IN_PROGRESS для подтвержения");
        }

        order.setStatus(OrderStatus.REVIEW);
        order.setFinalFileUrl(finalFileUrl);
        orderRepository.save(order);

    }

    @Override
    @Transactional
    public void completeOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Заказ не найден - " + orderId));

        if (order.getStatus() != OrderStatus.REVIEW) {
            throw new IllegalStateException("Заказ должен быть REVIEW для завершения");
        }

        order.setStatus(OrderStatus.COMPLETED);
        order.setCompletedAt(LocalDateTime.now());

        User artist = order.getArtist();
        artist.setBalance(artist.getBalance().add(order.getPrice()));
        userRepository.save(artist);

        orderRepository.save(order);

    }

    @Override
    @Transactional
    public void cancelOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Заказ не найден - " + orderId));

        if (order.getStatus() == OrderStatus.COMPLETED) {
            throw new IllegalStateException("Невозможно отменить заказ");
        }

        if (order.getStatus() != OrderStatus.CANCELLED) {
            User customer = order.getCustomer();
            customer.setBalance(customer.getBalance().add(order.getPrice()));
            userRepository.save(customer);
        }

        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);

    }

    @Override
    public BigDecimal getTotalEarnings(Long artistId) {
        return orderRepository.getTotalEarnings(artistId);
    }

    @Override
    public long getCompletedOrdersCount(Long artistId) {
        return orderRepository.getCompletedOrdersCount(artistId);
    }

    private OrderDto mapToDto(Order order) {
        return OrderDto.builder()
                .id(order.getId())
                .customerId(order.getCustomer().getId())
                .customerName(order.getCustomer().getUsername())
                .artistId(order.getArtist().getId())
                .artistName(order.getArtist().getUsername())
                .status(order.getStatus())
                .description(order.getDescription())
                .price(order.getPrice())
                .finalFileUrl(order.getFinalFileUrl())
                .createdAt(order.getCreatedAt())
                .completedAt(order.getCompletedAt())
                .build();
    }
}
