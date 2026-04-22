package by.step.repository;

import by.step.entity.Order;
import by.step.entity.User;
import by.step.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order,Long> {

    // JPA Methods

    Page<Order> findByCustomer(User customer, Pageable pageable);

    Page<Order> findByArtist(User artist, Pageable pageable);

    Page<Order> findByStatus(OrderStatus status, Pageable pageable);

    List<Order> findByArtistAndStatusIn(User artist, List<OrderStatus> statuses);

    // HQL Queries

    @Query("SELECT o FROM Order o WHERE o.customer.id = :customerId AND o.status = :status")
    List<Order> findByCustomerAndStatus(@Param("customerId") Long customerId,
                                        @Param("status") OrderStatus status);

    @Query("SELECT o FROM Order o WHERE o.createdAt BETWEEN :start AND :end")
    List<Order> findByCreatedAtBetween(@Param("start") LocalDateTime start,
                                       @Param("end") LocalDateTime end);

    @Query("SELECT SUM(o.price) FROM Order o WHERE o.artist.id = :artistId AND o.status = 'COMPLETED'")
    BigDecimal getTotalEarnings(@Param("artistId") Long artistId);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.artist.id = :artistId AND o.status = 'COMPLETED'")
    long getCompletedOrdersCount(@Param("artistId") Long artistId);

    // Modifying Queries

    @Modifying
    @Transactional
    @Query("UPDATE Order o SET o.status = :status WHERE o.id = :orderId")
    void updateStatus(@Param("orderId") Long orderId,
                      @Param("status") OrderStatus status);

    @Modifying
    @Transactional
    @Query("UPDATE Order o SET o.finalFileUrl = :fileUrl WHERE o.id = :orderId")
    void updateFinalFile(@Param("orderId") Long orderId,
                         @Param("fileUrl") String fileUrl);

    @Modifying
    @Transactional
    @Query("UPDATE Order o SET o.completedAt = :completedAt WHERE o.id = :orderId")
    void updateCompletedAt(@Param("orderId") Long orderId,
                           @Param("completedAt") LocalDateTime completedAt);
    }

    // Native SQL Queries

    // Pagination


