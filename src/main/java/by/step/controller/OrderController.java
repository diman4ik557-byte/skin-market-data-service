package by.step.controller;

import by.step.dto.OrderDto;
import by.step.enums.OrderStatus;
import by.step.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

/**
 * REST контроллер для работы с заказами.
 * Предоставляет API для создания, поиска, изменения статусов и управления заказами.
 *
 * @author Skin Market Team
 * @version 1.0
 */
@Slf4j
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    /**
     * Создает новый заказ.
     *
     * @param customerId ID заказчика
     * @param artistId ID художника
     * @param description описание заказа
     * @param price цена заказа
     * @return созданный заказ
     */
    @PostMapping
    public ResponseEntity<OrderDto> createOrder(
            @RequestParam Long customerId,
            @RequestParam Long artistId,
            @RequestParam String description,
            @RequestParam BigDecimal price) {
        log.info("REST запрос: создание заказа customer={}, artist={}, price={}", customerId, artistId, price);
        OrderDto order = orderService.createOrder(customerId, artistId, description, price);
        return ResponseEntity.ok(order);
    }

    /**
     * Находит заказ по ID.
     *
     * @param orderId ID заказа
     * @return найденный заказ
     */
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderDto> findById(@PathVariable Long orderId) {
        log.debug("REST запрос: поиск заказа по id={}", orderId);
        OrderDto order = orderService.findById(orderId);
        return ResponseEntity.ok(order);
    }

    /**
     * Находит все заказы заказчика с пагинацией.
     *
     * @param customerId ID заказчика
     * @param pageable параметры пагинации
     * @return страница с заказами заказчика
     */
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<Page<OrderDto>> findByCustomer(
            @PathVariable Long customerId,
            Pageable pageable) {
        log.debug("REST запрос: поиск заказов заказчика {}", customerId);
        Page<OrderDto> orders = orderService.findByCustomer(customerId, pageable);
        return ResponseEntity.ok(orders);
    }

    /**
     * Находит все заказы художника с пагинацией.
     *
     * @param artistId ID художника
     * @param pageable параметры пагинации
     * @return страница с заказами художника
     */
    @GetMapping("/artist/{artistId}")
    public ResponseEntity<Page<OrderDto>> findByArtist(
            @PathVariable Long artistId,
            Pageable pageable) {
        log.debug("REST запрос: поиск заказов художника {}", artistId);
        Page<OrderDto> orders = orderService.findByArtist(artistId, pageable);
        return ResponseEntity.ok(orders);
    }

    /**
     * Находит заказы по статусу с пагинацией.
     *
     * @param status статус заказа (NEW, IN_PROGRESS, REVIEW, COMPLETED, CANCELLED)
     * @param pageable параметры пагинации
     * @return страница с заказами указанного статуса
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<Page<OrderDto>> findByStatus(
            @PathVariable OrderStatus status,
            Pageable pageable) {
        log.debug("REST запрос: поиск заказов по статусу {}", status);
        Page<OrderDto> orders = orderService.findByStatus(status, pageable);
        return ResponseEntity.ok(orders);
    }

    /**
     * Обновляет статус заказа.
     *
     * @param orderId ID заказа
     * @param status новый статус
     * @return пустой ответ при успехе
     */
    @PutMapping("/{orderId}/status")
    public ResponseEntity<Void> updateStatus(
            @PathVariable Long orderId,
            @RequestParam OrderStatus status) {
        log.info("REST запрос: обновление статуса заказа {} на {}", orderId, status);
        orderService.updateStatus(orderId, status);
        return ResponseEntity.ok().build();
    }

    /**
     * Начинает выполнение заказа.
     *
     * @param orderId ID заказа
     * @return пустой ответ при успехе
     */
    @PostMapping("/{orderId}/start")
    public ResponseEntity<Void> startOrder(@PathVariable Long orderId) {
        log.info("REST запрос: начало выполнения заказа {}", orderId);
        orderService.startOrder(orderId);
        return ResponseEntity.ok().build();
    }

    /**
     * Отправляет заказ на проверку заказчику.
     *
     * @param orderId ID заказа
     * @param finalFileUrl URL готового файла для проверки
     * @return пустой ответ при успехе
     */
    @PostMapping("/{orderId}/submit-review")
    public ResponseEntity<Void> submitForReview(
            @PathVariable Long orderId,
            @RequestParam String finalFileUrl) {
        log.info("REST запрос: отправка заказа {} на проверку", orderId);
        orderService.submitForReview(orderId, finalFileUrl);
        return ResponseEntity.ok().build();
    }

    /**
     * Завершает заказ.
     *
     * @param orderId ID заказа
     * @return пустой ответ при успехе
     */
    @PostMapping("/{orderId}/complete")
    public ResponseEntity<Void> completeOrder(@PathVariable Long orderId) {
        log.info("REST запрос: завершение заказа {}", orderId);
        orderService.completeOrder(orderId);
        return ResponseEntity.ok().build();
    }

    /**
     * Отменяет заказ.
     *
     * @param orderId ID заказа
     * @return пустой ответ при успехе
     */
    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<Void> cancelOrder(@PathVariable Long orderId) {
        log.info("REST запрос: отмена заказа {}", orderId);
        orderService.cancelOrder(orderId);
        return ResponseEntity.ok().build();
    }

    /**
     * Возвращает общий заработок художника.
     *
     * @param artistId ID художника
     * @return сумма заработка
     */
    @GetMapping("/artist/{artistId}/earnings")
    public ResponseEntity<BigDecimal> getTotalEarnings(@PathVariable Long artistId) {
        log.debug("REST запрос: получение заработка художника {}", artistId);
        BigDecimal earnings = orderService.getTotalEarnings(artistId);
        return ResponseEntity.ok(earnings);
    }
}