package by.step.repository;

import by.step.entity.Message;
import by.step.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {

    // JPA Methods

    Page<Message> findByOrder(Order order, Pageable pageable);

    List<Message> findByOrderAndIsPreviewTrue(Order order);

    List<Message> findByOrderOrderBySentAtAsc(Order order);

    void deleteByOrder(Order order);

    // HQL Queries

    @Query("SELECT m FROM Message m WHERE m.order.id = :orderId AND m.sender.id = :senderId")
    List<Message> findByOrderAndSender(@Param("orderId") Long orderId,
                                       @Param("senderId") Long senderId);

    @Query("SELECT m FROM Message m WHERE m.order.id = :orderId AND m.attachmentUrl IS NOT NULL")
    List<Message> findMessagesWithAttachments(@Param("orderId") Long orderId);

    @Query("SELECT COUNT(m) FROM Message m WHERE m.order.id = :orderId AND m.isPreview = true")
    long countPreviewMessages(@Param("orderId") Long orderId);

    // Modifying Queries

    // Native SQL Queries

    @Query(value = "SELECT * FROM messages WHERE order_id = :orderId ORDER BY sent_at DESC LIMIT :limit",
            nativeQuery = true)
    List<Message> findLastMessages(@Param("orderId") Long orderId,
                                   @Param("limit") int limit);

    // Pagination
}
