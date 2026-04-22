package by.step.repository;

import by.step.DataServiceApplication;
import by.step.entity.Message;
import by.step.entity.Order;
import by.step.entity.User;
import by.step.enums.OrderStatus;
import by.step.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Disabled("Testcontainers временно отключены")
@SpringBootTest(classes = DataServiceApplication.class)
@ActiveProfiles("test")
@Transactional
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class MessageRepositoryTest extends PostgresTestcontainersBase  {

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    private Order testOrder;
    private User customer;
    private User artist;

    @BeforeEach
    void setUp() {
        customer = User.builder()
                .username("customer_" + System.currentTimeMillis())
                .email("customer_" + System.currentTimeMillis() + "@example.com")
                .password("encoded123")
                .role(UserRole.USER)
                .balance(BigDecimal.valueOf(1000))
                .registeredAt(LocalDateTime.now())
                .build();
        customer = userRepository.save(customer);

        artist = User.builder()
                .username("artist_" + System.currentTimeMillis())
                .email("artist_" + System.currentTimeMillis() + "@example.com")
                .password("encoded123")
                .role(UserRole.ARTIST)
                .balance(BigDecimal.valueOf(500))
                .registeredAt(LocalDateTime.now())
                .build();
        artist = userRepository.save(artist);

        // Создаем тестовый заказ
        testOrder = Order.builder()
                .customer(customer)
                .artist(artist)
                .status(OrderStatus.NEW)
                .description("Test order for messages")
                .price(BigDecimal.valueOf(100))
                .createdAt(LocalDateTime.now())
                .build();
        testOrder = orderRepository.save(testOrder);
    }

    @Test
    void findById() {
        // Создаем тестовое сообщение
        Message testMessage = Message.builder()
                .order(testOrder)
                .sender(customer)
                .content("Test message content")
                .isPreview(false)
                .sentAt(LocalDateTime.now())
                .build();
        Message saved = messageRepository.save(testMessage);

        Message found = messageRepository.findById(saved.getId()).orElseThrow();
        assertThat(found).isNotNull();
        assertThat(found.getContent()).isEqualTo("Test message content");
    }

    @Test
    void findByOrder() {
        // Создаем тестовое сообщение
        Message testMessage = Message.builder()
                .order(testOrder)
                .sender(customer)
                .content("Test message")
                .isPreview(false)
                .sentAt(LocalDateTime.now())
                .build();
        messageRepository.save(testMessage);

        Pageable pageable = PageRequest.of(0, 10);
        Page<Message> messages = messageRepository.findByOrder(testOrder, pageable);

        assertThat(messages.getContent()).isNotEmpty();
        assertThat(messages.getContent())
                .extracting(Message::getOrder)
                .extracting(Order::getId)
                .containsOnly(testOrder.getId());
    }

    @Test
    void findByOrderAndIsPreviewTrue() {
        // Создаем сообщение-предпросмотр
        Message preview = Message.builder()
                .order(testOrder)
                .sender(artist)
                .content("Preview message")
                .isPreview(true)
                .sentAt(LocalDateTime.now())
                .build();
        messageRepository.save(preview);

        List<Message> previews = messageRepository.findByOrderAndIsPreviewTrue(testOrder);

        assertThat(previews).isNotEmpty();
        assertThat(previews)
                .extracting(Message::getIsPreview)
                .containsOnly(true);
    }

    @Test
    void findByOrderOrderBySentAtAsc() {
        // Создаем несколько сообщений
        Message msg1 = Message.builder()
                .order(testOrder)
                .sender(customer)
                .content("First message")
                .isPreview(false)
                .sentAt(LocalDateTime.now().minusMinutes(5))
                .build();
        Message msg2 = Message.builder()
                .order(testOrder)
                .sender(artist)
                .content("Second message")
                .isPreview(false)
                .sentAt(LocalDateTime.now())
                .build();
        messageRepository.save(msg1);
        messageRepository.save(msg2);

        List<Message> messages = messageRepository.findByOrderOrderBySentAtAsc(testOrder);

        assertThat(messages).isNotEmpty();
        for (int i = 0; i < messages.size() - 1; i++) {
            assertThat(messages.get(i).getSentAt())
                    .isBeforeOrEqualTo(messages.get(i + 1).getSentAt());
        }
    }

    @Test
    void findByOrderAndSender() {
        // Создаем сообщение от customer
        Message testMessage = Message.builder()
                .order(testOrder)
                .sender(customer)
                .content("Message from customer")
                .isPreview(false)
                .sentAt(LocalDateTime.now())
                .build();
        messageRepository.save(testMessage);

        List<Message> messages = messageRepository.findByOrderAndSender(testOrder.getId(), customer.getId());

        assertThat(messages).isNotEmpty();
        assertThat(messages)
                .extracting(Message::getSender)
                .extracting(User::getId)
                .containsOnly(customer.getId());
    }

    @Test
    void findMessagesWithAttachments() {
        // Создаем сообщение с вложением
        Message withAttachment = Message.builder()
                .order(testOrder)
                .sender(artist)
                .content("Message with attachment")
                .attachmentUrl("/uploads/test.png")
                .isPreview(false)
                .sentAt(LocalDateTime.now())
                .build();
        messageRepository.save(withAttachment);

        List<Message> messagesWithAttachments = messageRepository.findMessagesWithAttachments(testOrder.getId());

        assertThat(messagesWithAttachments).isNotNull();
        assertThat(messagesWithAttachments).isNotEmpty();
        assertThat(messagesWithAttachments.get(0).getAttachmentUrl()).isNotNull();
    }

    @Test
    void checkCountPreviewMessages() {
        // Создаем несколько предпросмотров
        Message preview1 = Message.builder()
                .order(testOrder)
                .sender(artist)
                .content("Preview 1")
                .isPreview(true)
                .sentAt(LocalDateTime.now())
                .build();
        Message preview2 = Message.builder()
                .order(testOrder)
                .sender(artist)
                .content("Preview 2")
                .isPreview(true)
                .sentAt(LocalDateTime.now())
                .build();
        messageRepository.save(preview1);
        messageRepository.save(preview2);

        long count = messageRepository.countPreviewMessages(testOrder.getId());
        assertThat(count).isEqualTo(2);
    }

    @Test
    void findLastMessages() {
        // Создаем несколько сообщений
        for (int i = 0; i < 7; i++) {
            Message msg = Message.builder()
                    .order(testOrder)
                    .sender(customer)
                    .content("Message " + i)
                    .isPreview(false)
                    .sentAt(LocalDateTime.now().minusMinutes(i))
                    .build();
            messageRepository.save(msg);
        }

        List<Message> lastMessages = messageRepository.findLastMessages(testOrder.getId(), 5);

        assertThat(lastMessages).isNotEmpty();
        assertThat(lastMessages.size()).isLessThanOrEqualTo(5);
    }

    @Test
    @Transactional
    void checkSave() {
        Message message = Message.builder()
                .order(testOrder)
                .sender(customer)
                .content("Новое тестовое сообщение")
                .isPreview(false)
                .sentAt(LocalDateTime.now())
                .build();

        Message saved = messageRepository.save(message);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getContent()).isEqualTo("Новое тестовое сообщение");
        assertThat(saved.getSender().getId()).isEqualTo(customer.getId());
    }

    @Test
    @Transactional
    void checkDeleteByOrder() {
        // Создаем сообщения для заказа
        Message msg1 = Message.builder()
                .order(testOrder)
                .sender(customer)
                .content("Message to delete 1")
                .isPreview(false)
                .sentAt(LocalDateTime.now())
                .build();
        Message msg2 = Message.builder()
                .order(testOrder)
                .sender(artist)
                .content("Message to delete 2")
                .isPreview(false)
                .sentAt(LocalDateTime.now())
                .build();
        messageRepository.save(msg1);
        messageRepository.save(msg2);

        // Проверяем, что сообщения есть
        List<Message> beforeDelete = messageRepository.findByOrderOrderBySentAtAsc(testOrder);
        assertThat(beforeDelete).hasSize(2);

        // Удаляем все сообщения заказа
        messageRepository.deleteByOrder(testOrder);

        // Проверяем, что сообщений нет
        List<Message> afterDelete = messageRepository.findByOrderOrderBySentAtAsc(testOrder);
        assertThat(afterDelete).isEmpty();
    }
}
