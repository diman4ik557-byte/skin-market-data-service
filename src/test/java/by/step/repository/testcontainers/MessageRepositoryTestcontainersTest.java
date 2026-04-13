package by.step.repository.testcontainers;

import by.step.DataServiceApplication;
import by.step.entity.Message;
import by.step.entity.Order;
import by.step.entity.User;
import by.step.repository.MessageRepository;
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

import java.util.List;

@SpringBootTest(classes = DataServiceApplication.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
class MessageRepositoryTestcontainersTest extends PostgresTestcontainersBase {

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void findById() {
        Message message = messageRepository.findById(1L).orElseThrow();
        Assertions.assertThat(message).isNotNull();
        Assertions.assertThat(message.getContent()).isEqualTo("Здравствуйте! Хочу заказать гнома пивовара");
    }

    @Test
    void findByOrder() {
        Order order = orderRepository.findById(1L).orElseThrow();
        Pageable pageable = PageRequest.of(0, 10);
        Page<Message> messages = messageRepository.findByOrder(order, pageable);

        Assertions.assertThat(messages.getContent()).isNotEmpty();
        Assertions.assertThat(messages.getContent())
                .extracting(Message::getOrder)
                .extracting(Order::getId)
                .containsOnly(1L);
    }

    @Test
    void findByOrderAndIsPreviewTrue() {
        Order order = orderRepository.findById(2L).orElseThrow();
        List<Message> previews = messageRepository.findByOrderAndIsPreviewTrue(order);

        Assertions.assertThat(previews).isNotEmpty();
        Assertions.assertThat(previews)
                .extracting(Message::getIsPreview)
                .containsOnly(true);
    }

    @Test
    void findByOrderOrderBySentAtAsc() {
        Order order = orderRepository.findById(1L).orElseThrow();
        List<Message> messages = messageRepository.findByOrderOrderBySentAtAsc(order);

        Assertions.assertThat(messages).isNotEmpty();
        for (int i = 0; i < messages.size() - 1; i++) {
            Assertions.assertThat(messages.get(i).getSentAt())
                    .isBeforeOrEqualTo(messages.get(i + 1).getSentAt());
        }
    }

    @Test
    void findByOrderAndSender() {
        List<Message> messages = messageRepository.findByOrderAndSender(1L, 1L);

        Assertions.assertThat(messages).isNotEmpty();
        Assertions.assertThat(messages)
                .extracting(Message::getSender)
                .extracting(User::getId)
                .containsOnly(1L);
    }

    @Test
    void findMessagesWithAttachments() {
        Order order = orderRepository.findById(2L).orElseThrow();

        List<Message> messagesWithAttachments = messageRepository.findMessagesWithAttachments(2L);

        Assertions.assertThat(messagesWithAttachments).isNotNull();
    }

    @Test
    void checkCountPreviewMessages() {
        long count = messageRepository.countPreviewMessages(2L);
        Assertions.assertThat(count).isEqualTo(1);
    }

    @Test
    void findLastMessages() {
        List<Message> lastMessages = messageRepository.findLastMessages(1L, 5);

        Assertions.assertThat(lastMessages).isNotEmpty();
        Assertions.assertThat(lastMessages.size()).isLessThanOrEqualTo(5);
    }

    @Test
    @Transactional
    void checkSave() {
        Order order = orderRepository.findById(1L).orElseThrow();
        User sender = userRepository.findById(1L).orElseThrow();

        Message message = Message.builder()
                .order(order)
                .sender(sender)
                .content("Новое тестовое сообщение")
                .isPreview(false)
                .build();

        Message saved = messageRepository.save(message);

        Assertions.assertThat(saved.getId()).isNotNull();
        Assertions.assertThat(saved.getContent()).isEqualTo("Новое тестовое сообщение");
        Assertions.assertThat(saved.getSender().getId()).isEqualTo(1L);
    }

    @Test
    @Transactional
    void checkDeleteByOrder() {
        Order order = orderRepository.findById(3L).orElseThrow();
        messageRepository.deleteByOrder(order);

        List<Message> messages = messageRepository.findByOrderOrderBySentAtAsc(order);
        Assertions.assertThat(messages).isEmpty();
    }
}