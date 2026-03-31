package by.step.controller;

import by.step.dto.OrderDto;
import by.step.entity.enums.OrderStatus;
import by.step.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderDto> createOrder(
            @RequestParam Long customerId,
            @RequestParam Long artistId,
            @RequestParam String description,
            @RequestParam BigDecimal price) {
        OrderDto order = orderService.createOrder(customerId, artistId, description, price);
        return ResponseEntity.ok(order);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderDto> findById(@PathVariable Long orderId) {
        OrderDto order = orderService.findById(orderId);
        return ResponseEntity.ok(order);
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<Page<OrderDto>> findByCustomer(
            @PathVariable Long customerId,
            Pageable pageable) {
        return ResponseEntity.ok(orderService.findByCustomer(customerId, pageable));
    }

    @GetMapping("/artist/{artistId}")
    public ResponseEntity<Page<OrderDto>> findByArtist(
            @PathVariable Long artistId,
            Pageable pageable) {
        return ResponseEntity.ok(orderService.findByArtist(artistId, pageable));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<Page<OrderDto>> findByStatus(
            @PathVariable OrderStatus status,
            Pageable pageable) {
        return ResponseEntity.ok(orderService.findByStatus(status, pageable));
    }

    @GetMapping("/artist/{artistId}/active")
    public ResponseEntity<List<OrderDto>> findActiveOrdersByArtist(@PathVariable Long artistId) {
        return ResponseEntity.ok(orderService.findActiveOrdersByArtist(artistId));
    }

    @PutMapping("/{orderId}/status")
    public ResponseEntity<Void> updateStatus(
            @PathVariable Long orderId,
            @RequestParam OrderStatus status) {
        orderService.updateStatus(orderId, status);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{orderId}/final-file")
    public ResponseEntity<Void> updateFinalFile(
            @PathVariable Long orderId,
            @RequestParam String fileUrl) {
        orderService.updateFinalFile(orderId, fileUrl);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{orderId}/start")
    public ResponseEntity<Void> startOrder(@PathVariable Long orderId) {
        orderService.startOrder(orderId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{orderId}/submit-review")
    public ResponseEntity<Void> submitForReview(
            @PathVariable Long orderId,
            @RequestParam String finalFileUrl) {
        orderService.submitForReview(orderId, finalFileUrl);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{orderId}/complete")
    public ResponseEntity<Void> completeOrder(@PathVariable Long orderId) {
        orderService.completeOrder(orderId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<Void> cancelOrder(@PathVariable Long orderId) {
        orderService.cancelOrder(orderId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/artist/{artistId}/earnings")
    public ResponseEntity<BigDecimal> getTotalEarnings(@PathVariable Long artistId) {
        return ResponseEntity.ok(orderService.getTotalEarnings(artistId));
    }

    @GetMapping("/artist/{artistId}/completed-count")
    public ResponseEntity<Long> getCompletedOrdersCount(@PathVariable Long artistId) {
        return ResponseEntity.ok(orderService.getCompletedOrdersCount(artistId));
    }
}