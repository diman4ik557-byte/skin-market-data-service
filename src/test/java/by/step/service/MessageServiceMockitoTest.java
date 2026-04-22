package by.step.service;

import by.step.dto.MessageDto;
import by.step.entity.*;
import by.step.enums.OrderStatus;
import by.step.enums.StudioRole;
import by.step.enums.StudioMemberStatus;
import by.step.enums.UserRole;
import by.step.repository.*;
import by.step.service.impl.MessageServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Тесты сервиса сообщений")
class MessageServiceMockitoTest {

    @Mock
    private MessageRepository messageRepository;
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private StudioRepository studioRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private StudioMemberRepository studioMemberRepository;
    @Mock
    private ArtistProfileRepository artistProfileRepository;

    @InjectMocks
    private MessageServiceImpl messageService;

    private User customer;
    private User artist;
    private User manager;
    private Order testOrder;
    private Studio testStudio;
    private ArtistProfile artistProfile;
    private Profile artistProfileEntity;
    private StudioMember managerMember;
    private StudioMember artistMember;
    private Message testMessage;

    @BeforeEach
    void setUp() {
        customer = User.builder()
                .id(1L)
                .username("customer")
                .email("customer@example.com")
                .role(UserRole.USER)
                .balance(BigDecimal.valueOf(1000))
                .build();

        artist = User.builder()
                .id(2L)
                .username("artist")
                .email("artist@example.com")
                .role(UserRole.ARTIST)
                .balance(BigDecimal.valueOf(500))
                .build();

        manager = User.builder()
                .id(3L)
                .username("manager")
                .email("manager@example.com")
                .role(UserRole.ARTIST)
                .balance(BigDecimal.ZERO)
                .build();

        testOrder = Order.builder()
                .id(1L)
                .customer(customer)
                .artist(artist)
                .status(OrderStatus.NEW)
                .description("Test order")
                .price(BigDecimal.valueOf(100))
                .createdAt(LocalDateTime.now())
                .build();

        testStudio = Studio.builder()
                .id(1L)
                .name("Test Studio")
                .description("Test Description")
                .manager(manager)
                .build();

        artistProfileEntity = Profile.builder()
                .id(1L)
                .user(artist)
                .isArtist(true)
                .build();

        artistProfile = ArtistProfile.builder()
                .id(1L)
                .profile(artistProfileEntity)
                .build();

        managerMember = StudioMember.builder()
                .id(1L)
                .studio(testStudio)
                .member(artistProfile)
                .role(StudioRole.MANAGER)
                .status(StudioMemberStatus.APPROVED)
                .build();

        artistMember = StudioMember.builder()
                .id(2L)
                .studio(testStudio)
                .member(artistProfile)
                .role(StudioRole.ARTIST)
                .status(StudioMemberStatus.APPROVED)
                .build();

        testMessage = Message.builder()
                .id(1L)
                .order(testOrder)
                .sender(customer)
                .content("Test message content")
                .isPreview(false)
                .isRedirected(false)
                .sentAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Отправка файла в заказ - успех")
    void sendAttachment_Success() {
        Message attachmentMessage = Message.builder()
                .id(2L)
                .order(testOrder)
                .sender(artist)
                .attachmentUrl("/uploads/file.png")
                .isPreview(false)
                .sentAt(LocalDateTime.now())
                .build();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(userRepository.findById(2L)).thenReturn(Optional.of(artist));
        when(messageRepository.save(any(Message.class))).thenReturn(attachmentMessage);

        MessageDto result = messageService.sendAttachment(1L, 2L, "/uploads/file.png");

        assertThat(result).isNotNull();
        assertThat(result.getAttachmentUrl()).isEqualTo("/uploads/file.png");
        verify(messageRepository, times(1)).save(any(Message.class));
    }

    @Test
    @DisplayName("Отправка вложения - отправитель не найден")
    void sendAttachment_SenderNotFound() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(userRepository.findById(1L)).thenReturn(Optional.empty());    // отправитель НЕ найден

        assertThatThrownBy(() -> messageService.sendAttachment(1L, 1L, "/path/to/file.png"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Отправка файла - заказ не найден")
    void sendAttachment_OrderNotFound() {
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> messageService.sendAttachment(999L, 1L, "file.png"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Заказ не найден -  " + 999L);
    }

    @Test
    @DisplayName("Получение всех предпросмотров по заказу")
    void getPreviewMessages_Success() {
        Message preview1 = Message.builder()
                .id(2L)
                .order(testOrder)
                .sender(artist)
                .content("Preview 1")
                .isPreview(true)
                .build();
        Message preview2 = Message.builder()
                .id(3L)
                .order(testOrder)
                .sender(artist)
                .content("Preview 2")
                .isPreview(true)
                .build();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(messageRepository.findByOrderAndIsPreviewTrue(testOrder)).thenReturn(List.of(preview1, preview2));

        List<MessageDto> result = messageService.getPreviewMessages(1L);

        assertThat(result).hasSize(2);
        verify(messageRepository, times(1)).findByOrderAndIsPreviewTrue(testOrder);
    }

    @Test
    @DisplayName("Получение предпросмотров - заказ не найден")
    void getPreviewMessages_OrderNotFound() {
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> messageService.getPreviewMessages(999L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Заказ не найден - " + 999L);
    }

    @Test
    @DisplayName("Получение сообщений с вложениями по заказу")
    void getMessagesWithAttachments_Success() {
        Message attachment1 = Message.builder()
                .id(2L)
                .order(testOrder)
                .attachmentUrl("/file1.png")
                .build();
        Message attachment2 = Message.builder()
                .id(3L)
                .order(testOrder)
                .attachmentUrl("/file2.png")
                .build();

        when(messageRepository.findMessagesWithAttachments(1L)).thenReturn(List.of(attachment1, attachment2));

        List<MessageDto> result = messageService.getMessagesWithAttachments(1L);

        assertThat(result).hasSize(2);
        verify(messageRepository, times(1)).findMessagesWithAttachments(1L);
    }


    @Test
    @DisplayName("Отправка сообщения конкретному художнику от менеджера")
    void sendToArtist_Success() {
        Message redirectedMessage = Message.builder()
                .id(4L)
                .studio(testStudio)
                .sender(manager)
                .receiver(artist)
                .content("Message to artist")
                .isRedirected(true)
                .sentAt(LocalDateTime.now())
                .build();

        when(studioRepository.findById(1L)).thenReturn(Optional.of(testStudio));
        when(userRepository.findById(3L)).thenReturn(Optional.of(manager));
        when(userRepository.findById(2L)).thenReturn(Optional.of(artist));
        when(messageRepository.save(any(Message.class))).thenReturn(redirectedMessage);

        MessageDto result = messageService.sendToArtist(1L, 3L, 2L,
                "Message to artist", null);

        assertThat(result).isNotNull();
        assertThat(result.getReceiverId()).isEqualTo(2L);
        assertThat(result.getIsRedirected()).isTrue();
        verify(messageRepository, times(1)).save(any(Message.class));
    }

    @Test
    @DisplayName("Отправка сообщения художнику - студия не найдена")
    void sendToArtist_StudioNotFound() {
        when(studioRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> messageService.sendToArtist(999L, 3L,
                2L, "Message", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Студия не найдена - " + 999L);
    }

    @Test
    @DisplayName("Отправка художнику - отправитель не найден")
    void sendToArtist_SenderNotFound() {
        when(studioRepository.findById(1L)).thenReturn(Optional.of(testStudio));
        when(userRepository.findById(3L)).thenReturn(Optional.empty());    // отправитель НЕ найден

        assertThatThrownBy(() -> messageService.sendToArtist(1L, 3L, 2L, "Message", null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Отправка художнику - получатель не найден")
    void sendToArtist_ReceiverNotFound() {
        when(studioRepository.findById(1L)).thenReturn(Optional.of(testStudio));
        when(userRepository.findById(3L)).thenReturn(Optional.of(manager)); // отправитель найден
        when(userRepository.findById(2L)).thenReturn(Optional.empty());     // получатель НЕ найден

        assertThatThrownBy(() -> messageService.sendToArtist(1L, 3L, 2L, "Message", null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Перенаправление сообщения художнику - успех")
    void redirectToArtist_Success() {
        Message originalMessage = Message.builder()
                .id(1L)
                .studio(testStudio)
                .sender(customer)
                .content("Original message")
                .build();

        Message redirectedMessage = Message.builder()
                .id(5L)
                .studio(testStudio)
                .sender(manager)
                .receiver(artist)
                .content("Original message")
                .isRedirected(true)
                .build();

        when(messageRepository.findById(1L)).thenReturn(Optional.of(originalMessage));
        when(userRepository.findById(2L)).thenReturn(Optional.of(artist));
        when(userRepository.findById(3L)).thenReturn(Optional.of(manager));
        when(messageRepository.save(any(Message.class))).thenReturn(redirectedMessage);

        MessageDto result = messageService.redirectToArtist(1L, 2L, 3L);

        assertThat(result).isNotNull();
        assertThat(result.getIsRedirected()).isTrue();
        assertThat(result.getReceiverId()).isEqualTo(2L);
        verify(messageRepository, times(1)).save(any(Message.class));
    }

    @Test
    @DisplayName("Перенаправление сообщения - исходное сообщение не найдено")
    void redirectToArtist_MessageNotFound() {
        when(messageRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> messageService.redirectToArtist(999L,
                2L, 3L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Сообщение не найдено - " + 999L);
    }

    @Test
    @DisplayName("Перенаправление художнику - менеджер не найден")
    void redirectToArtist_ManagerNotFound() {
        when(messageRepository.findById(1L)).thenReturn(Optional.of(testMessage));
        when(userRepository.findById(2L)).thenReturn(Optional.of(artist)); // получатель найден
        when(userRepository.findById(3L)).thenReturn(Optional.empty());    // менеджер НЕ найден

        assertThatThrownBy(() -> messageService.redirectToArtist(1L, 2L, 3L))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Перенаправление художнику - получатель не найден")
    void redirectToArtist_ReceiverNotFound() {
        when(messageRepository.findById(1L)).thenReturn(Optional.of(testMessage));
        when(userRepository.findById(2L)).thenReturn(Optional.empty());    // получатель НЕ найден

        assertThatThrownBy(() -> messageService.redirectToArtist(1L, 2L, 3L))
                .isInstanceOf(IllegalArgumentException.class);
    }

    // sendMessage

    @Test
    @DisplayName("Отправка обычного сообщения в заказ - успех")
    void sendMessage_Success() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(userRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(messageRepository.save(any(Message.class))).thenReturn(testMessage);

        MessageDto result = messageService.sendMessage(1L, 1L, "Test content");

        assertThat(result).isNotNull();
        verify(messageRepository).save(any(Message.class));
    }

    @Test
    @DisplayName("Отправка сообщения - пустой текст (ожидание ошибки валидации)")
    void sendMessage_EmptyContent() {
        assertThatThrownBy(() -> messageService.sendMessage(1L, 1L, ""))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Отправка обычного сообщения - заказ не найден")
    void sendMessage_OrderNotFound() {
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> messageService.sendMessage(999L, 1L, "Test"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Отправка обычного сообщения - пользователь не найден")
    void sendMessage_UserNotFound() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> messageService.sendMessage(1L, 999L, "Test"))
                .isInstanceOf(IllegalArgumentException.class);
    }
    @Test
    @DisplayName("sendMessage: пользователь не участвует в заказе")
    void sendMessage_AccessDenied() {
        User stranger = User.builder().id(999L).username("stranger").build();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(userRepository.findById(999L)).thenReturn(Optional.of(stranger));

        assertThatThrownBy(() -> messageService.sendMessage(1L, 999L, "I want to spy"))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("Пользователь не участвует в этом заказе");
    }



    // sendPreview
    @Test
    @DisplayName("Отправка предпросмотра (Preview) - успех")
    void sendPreview_Success() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(userRepository.findById(2L)).thenReturn(Optional.of(artist));
        when(messageRepository.save(any(Message.class))).thenReturn(testMessage);

        MessageDto result = messageService.sendPreview(1L, 2L, "Preview text", "/url.jpg");

        assertThat(result).isNotNull();
        verify(messageRepository).save(any(Message.class));
    }

    @Test
    @DisplayName("Отправка предпросмотра - заказ не найден")
    void sendPreview_OrderNotFound() {
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> messageService.sendPreview(999L, 2L, "text", "/url.jpg"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Отправка предпросмотра - пользователь не найден")
    void sendPreview_UserNotFound() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> messageService.sendPreview(1L, 999L, "text", "/url.jpg"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    // sendToStudio
    @Test
    @DisplayName("Отправка сообщения в студию - успех")
    void sendToStudio_Success() {
        when(studioRepository.findById(1L)).thenReturn(Optional.of(testStudio));
        when(userRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(messageRepository.save(any(Message.class))).thenReturn(testMessage);

        MessageDto result = messageService.sendToStudio(1L, 1L, "To studio", "/att.jpg");

        assertThat(result).isNotNull();
        verify(messageRepository).save(any(Message.class));
    }

    @Test
    @DisplayName("Отправка сообщения в студию - студия не найдена")
    void sendToStudio_StudioNotFound() {
        when(studioRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> messageService.sendToStudio(999L, 1L, "text", null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Отправка сообщения в студию - пользователь не найден")
    void sendToStudio_UserNotFound() {
        when(studioRepository.findById(1L)).thenReturn(Optional.of(testStudio));
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> messageService.sendToStudio(1L, 999L, "text", null))
                .isInstanceOf(IllegalArgumentException.class);
    }


    // getMessagesByOrder и getMessagesByStudio
    @Test
    @DisplayName("Получение сообщений по заказу - успех")
    void getMessagesByOrder_Success() {
        Page<Message> page = new PageImpl<>(List.of(testMessage));
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(messageRepository.findByOrder(eq(testOrder), any(Pageable.class))).thenReturn(page);

        Page<MessageDto> result = messageService.getMessagesByOrder(1L, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    @DisplayName("Получение сообщений по заказу - заказ не найден")
    void getMessagesByOrder_OrderNotFound() {
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> messageService.getMessagesByOrder(999L, PageRequest.of(0, 10)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Получение сообщений по студии - успех")
    void getMessagesByStudio_Success() {
        Page<Message> page = new PageImpl<>(List.of(testMessage));
        when(studioRepository.findById(1L)).thenReturn(Optional.of(testStudio));
        when(messageRepository.findByStudio(eq(testStudio), any(Pageable.class))).thenReturn(page);

        Page<MessageDto> result = messageService.getMessagesByStudio(1L, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    @DisplayName("Получение сообщений по студии - студия не найдена")
    void getMessagesByStudio_StudioNotFound() {
        when(studioRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> messageService.getMessagesByStudio(999L, PageRequest.of(0, 10)))
                .isInstanceOf(IllegalArgumentException.class);
    }


    // getUnassignedStudioMessages


    @Test
    @DisplayName("Получение неназначенных сообщений студии - успех")
    void getUnassignedStudioMessages_Success() {
        when(studioRepository.findById(1L)).thenReturn(Optional.of(testStudio));
        when(messageRepository.findByStudioAndReceiverIsNull(testStudio)).thenReturn(List.of(testMessage));

        List<MessageDto> result = messageService.getUnassignedStudioMessages(1L);

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("Получение неназначенных сообщений студии - студия не найдена")
    void getUnassignedStudioMessages_StudioNotFound() {
        when(studioRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> messageService.getUnassignedStudioMessages(999L))
                .isInstanceOf(IllegalArgumentException.class);
    }

    // getPreviewMessagesCount
    @Test
    @DisplayName("Получение количества предпросмотров - успех")
    void getPreviewMessagesCount_Success() {
        when(messageRepository.countPreviewMessages(1L)).thenReturn(5L);

        long count = messageService.getPreviewMessagesCount(1L);

        assertThat(count).isEqualTo(5L);
    }

    // canUserAccessOrderMessages
    @Test
    @DisplayName("Проверка прав на просмотр сообщений заказа - Заказчик")
    void canUserAccessOrderMessages_Customer() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        boolean result = messageService.canUserAccessOrderMessages(1L, customer.getId());
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("Проверка прав на просмотр сообщений заказа - Художник")
    void canUserAccessOrderMessages_Artist() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        boolean result = messageService.canUserAccessOrderMessages(1L, artist.getId());
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("Проверка прав на просмотр сообщений заказа - Пользователь без прав")
    void canUserAccessOrderMessages_NoAccess() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        boolean result = messageService.canUserAccessOrderMessages(1L, 999L);
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("Проверка прав - заказ не найден (Exception)")
    void canUserAccessOrderMessages_OrderNotFound() {
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        boolean result = messageService.canUserAccessOrderMessages(999L, 1L);
        assertThat(result).isFalse();
    }
}