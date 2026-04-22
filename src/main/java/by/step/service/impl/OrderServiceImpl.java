package by.step.service.impl;

import by.step.dto.OrderDto;
import by.step.entity.Order;
import by.step.entity.User;
import by.step.enums.OrderStatus;
import by.step.enums.UserRole;
import by.step.mapper.OrderMapper;
import by.step.repository.OrderRepository;
import by.step.repository.UserRepository;
import by.step.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final OrderMapper orderMapper = OrderMapper.INSTANCE;

    /**
     * Создает новый заказ.
     * Проверяет существование заказчика и художника, наличие средств у заказчика.
     * Списывает деньги с баланса заказчика при создании заказа.
     *
     * @param customerId ID заказчика
     * @param artistId ID художника
     * @param description описание заказа
     * @param price цена заказа
     * @return OrderDto созданного заказа
     * @throws IllegalArgumentException если заказчик или художник не найдены,
     *         пользователь не является художником, или недостаточно средств
     */
    @Override
    @Transactional
    public OrderDto createOrder(Long customerId, Long artistId, String description, BigDecimal price) {
        log.info("=== НАЧАЛО СОЗДАНИЯ ЗАКАЗА ===");
        log.info("Заказчик ID: {}, Художник ID: {}, Цена: {}", customerId, artistId, price);
        long startTime = System.currentTimeMillis();

        try {
            User customer = userRepository.findById(customerId)
                    .orElseThrow(() -> {
                        log.error("Заказчик с id={} не найден", customerId);
                        return new IllegalArgumentException("Заказчик не найден - " + customerId);
                    });
            log.debug("Заказчик найден: username={}, баланс={}", customer.getUsername(), customer.getBalance());

            User artist = userRepository.findById(artistId)
                    .orElseThrow(() -> {
                        log.error("Художник с id={} не найден", artistId);
                        return new IllegalArgumentException("Художник не найден - " + artistId);
                    });
            log.debug("Художник найден: username={}, роль={}", artist.getUsername(), artist.getRole());

            if (artist.getRole() != UserRole.ARTIST && artist.getRole() != UserRole.ADMIN) {
                log.warn("Пользователь {} не является художником", artist.getUsername());
                throw new IllegalArgumentException("Пользователь не является художником - " + artistId);
            }

            if (customer.getBalance().compareTo(price) < 0) {
                log.warn("Недостаточно средств у заказчика {}: баланс={}, цена={}",
                        customer.getUsername(), customer.getBalance(), price);
                throw new IllegalArgumentException("Недостаточно средств. Баланс: " + customer.getBalance());
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
            log.debug("С баланса заказчика списано {}, новый баланс: {}", price, customer.getBalance());

            Order saved = orderRepository.save(order);

            long elapsedTime = System.currentTimeMillis() - startTime;
            log.info("Заказ #{} создан успешно за {} мс", saved.getId(), elapsedTime);
            log.info("=== ЗАВЕРШЕНИЕ СОЗДАНИЯ ЗАКАЗА ===");

            return orderMapper.toDto(saved);

        } catch (Exception e) {
            log.error("Ошибка при создании заказа: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Находит заказ по ID.
     *
     * @param orderId ID заказа
     * @return OrderDto найденного заказа
     * @throws IllegalArgumentException если заказ не найден
     */
    @Override
    @Cacheable(value = "orderById", key = "#orderId")
    public OrderDto findById(Long orderId) {
        log.debug("Поиск заказа по ID (БД): {}", orderId);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Заказ не найден - " + orderId));
        return orderMapper.toDto(order);
    }

    /**
     * Находит все заказы заказчика с пагинацией.
     *
     * @param customerId ID заказчика
     * @param pageable параметры пагинации
     * @return страница с заказами заказчика
     * @throws IllegalArgumentException если заказчик не найден
     */
    @Override
    @Transactional(readOnly = true)
    public Page<OrderDto> findByCustomer(Long customerId, Pageable pageable) {
        log.info("Finding orders by customer: {}", customerId);
        User customer = userRepository.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Заказчик не найден - " + customerId));
        return orderRepository.findByCustomer(customer, pageable)
                .map(orderMapper::toDto);
    }


    /**
     * Находит все заказы заказчика в виде списка (без пагинации).
     *
     * @param customerId идентификатор заказчика
     * @return список заказов заказчика
     * @throws IllegalArgumentException если заказчик не найден
     */
    @Override
    @Transactional(readOnly = true)
    public List<OrderDto> findByCustomerAsList(Long customerId) {
        log.info("Finding orders by customer as list: {}", customerId);
        User customer = userRepository.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Заказчик не найден - " + customerId));
        return orderRepository.findByCustomer(customer, Pageable.unpaged())
                .map(orderMapper::toDto)
                .getContent();
    }

    /**
     * Находит все заказы художника с пагинацией.
     *
     * @param artistId ID художника
     * @param pageable параметры пагинации
     * @return страница с заказами художника
     * @throws IllegalArgumentException если художник не найден
     */
    @Override
    @Transactional(readOnly = true)
    public Page<OrderDto> findByArtist(Long artistId, Pageable pageable) {
        log.info("Finding orders by artist: {}", artistId);
        User artist = userRepository.findById(artistId)
                .orElseThrow(() -> new IllegalArgumentException("Художник не найден - " + artistId));
        return orderRepository.findByArtist(artist, pageable)
                .map(orderMapper::toDto);
    }

    /**
     * Находит заказы по статусу с пагинацией.
     *
     * @param status статус заказа
     * @param pageable параметры пагинации
     * @return страница с заказами указанного статуса
     */
    @Override
    @Transactional(readOnly = true)
    public Page<OrderDto> findByStatus(OrderStatus status, Pageable pageable) {
        log.info("Finding orders by status: {}", status);
        return orderRepository.findByStatus(status, pageable)
                .map(orderMapper::toDto);
    }

    /**
     * Находит активные заказы художника (NEW, IN_PROGRESS, REVIEW).
     *
     * @param artistId идентификатор художника
     * @return список активных заказов художника
     * @throws IllegalArgumentException если художник не найден
     */
    @Override
    @Transactional(readOnly = true)
    public List<OrderDto> findActiveOrdersByArtist(Long artistId) {
        log.info("Finding active orders by artist: {}", artistId);
        User artist = userRepository.findById(artistId)
                .orElseThrow(() -> new IllegalArgumentException("Художник не найден - " + artistId));

        List<Order> orders = orderRepository.findByArtistAndStatusIn(
                artist,
                List.of(OrderStatus.NEW, OrderStatus.IN_PROGRESS, OrderStatus.REVIEW)
        );

        return orders.stream()
                .map(orderMapper::toDto)
                .toList();
    }

    /**
     * Обновляет статус заказа.
     *
     * @param orderId ID заказа
     * @param status новый статус
     * @throws IllegalArgumentException если заказ не найден
     */
    @Override
    @Transactional
    public void updateStatus(Long orderId, OrderStatus status) {
        log.info("Updating order status: orderId={}, status={}", orderId, status);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Заказ не найден - " + orderId));
        order.setStatus(status);
        orderRepository.save(order);
    }

    /**
     * Обновляет URL финального файла заказа.
     *
     * @param orderId идентификатор заказа
     * @param fileUrl URL готового скина
     */
    @Override
    @Transactional
    public void updateFinalFile(Long orderId, String fileUrl) {
        log.info("Updating final file: orderId={}, fileUrl={}", orderId, fileUrl);
        orderRepository.updateFinalFile(orderId, fileUrl);
    }

    /**
     * Начинает выполнение заказа.
     * Изменяет статус с NEW на IN_PROGRESS.
     *
     * @param orderId ID заказа
     * @throws IllegalArgumentException если заказ не найден
     * @throws IllegalStateException если заказ не в статусе NEW
     */
    @Override
    @Transactional
    @CacheEvict(value = {"orderById", "ordersByCustomer", "ordersByArtist"}, key = "#orderId")
    public void startOrder(Long orderId) {
        log.info("Начало выполнения заказа #{}", orderId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> {
                    log.error("Заказ #{} не найден", orderId);
                    return new IllegalArgumentException("Заказ не найден - " + orderId);
                });

        log.debug("Текущий статус заказа #{}: {}", orderId, order.getStatus());

        if (order.getStatus() != OrderStatus.NEW) {
            log.warn("Невозможно начать заказ #{}: текущий статус {}, требуется NEW", orderId, order.getStatus());
            throw new IllegalStateException("Заказ должен быть в статусе NEW, текущий статус: " + order.getStatus());
        }

        order.setStatus(OrderStatus.IN_PROGRESS);
        orderRepository.save(order);

        evictOrderCaches(orderId);

        log.info("Заказ #{} переведен в статус IN_PROGRESS", orderId);
    }

    /**
     * Отправляет заказ на проверку заказчику.
     * Изменяет статус с IN_PROGRESS на REVIEW.
     *
     * @param orderId ID заказа
     * @param finalFileUrl URL готового файла для проверки
     * @throws IllegalArgumentException если заказ не найден
     * @throws IllegalStateException если заказ не в статусе IN_PROGRESS
     */
    @Override
    @Transactional
    public void submitForReview(Long orderId, String finalFileUrl) {
        log.info("Submitting order for review: {}", orderId);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Заказ не найден - " + orderId));

        if (order.getStatus() != OrderStatus.IN_PROGRESS) {
            throw new IllegalStateException("Заказ должен быть в статусе IN_PROGRESS, текущий статус: " + order.getStatus());
        }

        order.setStatus(OrderStatus.REVIEW);
        order.setFinalFileUrl(finalFileUrl);
        orderRepository.save(order);
    }

    /**
     * Завершает заказ.
     * Изменяет статус с REVIEW на COMPLETED и переводит деньги художнику.
     *
     * @param orderId ID заказа
     * @throws IllegalArgumentException если заказ не найден
     * @throws IllegalStateException если заказ не в статусе REVIEW
     */
    @Override
    @Transactional
    @CacheEvict(value = {"orderById", "ordersByCustomer", "ordersByArtist"}, key = "#orderId")
    public void completeOrder(Long orderId) {
        log.info("Completing order: {}", orderId);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Заказ не найден - " + orderId));

        if (order.getStatus() != OrderStatus.REVIEW) {
            throw new IllegalStateException("Заказ должен быть в статусе REVIEW, текущий статус: " + order.getStatus());
        }

        order.setStatus(OrderStatus.COMPLETED);
        order.setCompletedAt(LocalDateTime.now());

        User artist = order.getArtist();
        artist.setBalance(artist.getBalance().add(order.getPrice()));
        userRepository.save(artist);

        orderRepository.save(order);

        evictOrderCaches(orderId);
        log.info("Order {} completed", orderId);
    }

    /**
     * Отменяет заказ.
     * Возвращает деньги заказчику если заказ не был завершен.
     *
     * @param orderId ID заказа
     * @throws IllegalArgumentException если заказ не найден
     * @throws IllegalStateException если заказ уже завершен
     */
    @Override
    @Transactional
    public void cancelOrder(Long orderId) {
        log.info("Cancelling order: {}", orderId);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Заказ не найден - " + orderId));

        if (order.getStatus() == OrderStatus.COMPLETED) {
            throw new IllegalStateException("Нельзя отменить завершенный заказ");
        }

        if (order.getStatus() != OrderStatus.CANCELLED) {
            User customer = order.getCustomer();
            customer.setBalance(customer.getBalance().add(order.getPrice()));
            userRepository.save(customer);
        }

        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);
        log.info("Order {} cancelled", orderId);
    }

    /**
     * Возвращает общий заработок художника по завершенным заказам.
     *
     * @param artistId ID художника
     * @return сумма заработка
     */
    @Override
    @Transactional(readOnly = true)
    public BigDecimal getTotalEarnings(Long artistId) {
        log.info("Getting total earnings for artist: {}", artistId);
        BigDecimal earnings = orderRepository.getTotalEarnings(artistId);
        return earnings != null ? earnings : BigDecimal.ZERO;
    }

    /**
     * Возвращает количество завершенных заказов художника.
     *
     * @param artistId ID художника
     * @return количество завершенных заказов
     */
    @Override
    @Transactional(readOnly = true)
    public long getCompletedOrdersCount(Long artistId) {
        log.info("Getting completed orders count for artist: {}", artistId);
        return orderRepository.getCompletedOrdersCount(artistId);
    }

    private void evictOrderCaches(Long orderId) {
        log.debug("Очистка кэша для заказа: {}", orderId);
    }
}