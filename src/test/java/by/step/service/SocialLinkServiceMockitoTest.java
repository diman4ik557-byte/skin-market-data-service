package by.step.service;

import by.step.dto.SocialLinkDto;
import by.step.entity.Profile;
import by.step.entity.SocialLink;
import by.step.entity.User;
import by.step.enums.SocialPlatform;
import by.step.enums.UserRole;
import by.step.repository.ProfileRepository;
import by.step.repository.SocialLinkRepository;
import by.step.service.impl.SocialLinkServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Тесты сервиса социальных ссылок")
class SocialLinkServiceMockitoTest {

    @Mock
    private SocialLinkRepository socialLinkRepository;

    @Mock
    private ProfileRepository profileRepository;

    @InjectMocks
    private SocialLinkServiceImpl socialLinkService;

    private User testUser;
    private Profile testProfile;
    private SocialLink testSocialLink;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .role(UserRole.ARTIST)
                .balance(BigDecimal.ZERO)
                .build();

        testProfile = Profile.builder()
                .id(1L)
                .user(testUser)
                .bio("Test bio")
                .isArtist(true)
                .isStudio(false)
                .build();

        testSocialLink = SocialLink.builder()
                .id(1L)
                .profile(testProfile)
                .platform(SocialPlatform.VK)
                .userIdentifier("test_vk_id")
                .isPrimary(true)
                .build();
    }

    // addSocialLink

    @Test
    @DisplayName("Добавление социальной ссылки - успех")
    void addSocialLink_Success() {
        when(profileRepository.findById(1L)).thenReturn(Optional.of(testProfile));
        when(socialLinkRepository.findByProfileAndPlatform(testProfile, SocialPlatform.VK))
                .thenReturn(Optional.empty());
        when(socialLinkRepository.findByProfile(testProfile)).thenReturn(List.of());
        when(socialLinkRepository.save(any(SocialLink.class))).thenReturn(testSocialLink);

        SocialLinkDto result = socialLinkService.addSocialLink(1L, SocialPlatform.VK, "test_vk_id");

        assertThat(result).isNotNull();
        assertThat(result.getPlatform()).isEqualTo(SocialPlatform.VK);
        assertThat(result.getUserIdentifier()).isEqualTo("test_vk_id");
        assertThat(result.getIsPrimary()).isTrue();
        verify(socialLinkRepository, times(1)).save(any(SocialLink.class));
    }

    @Test
    @DisplayName("Добавление социальной ссылки - профиль не найден")
    void addSocialLink_ProfileNotFound() {
        when(profileRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> socialLinkService.addSocialLink(999L,
                SocialPlatform.VK, "id"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Профиль не найден");
    }

    @Test
    @DisplayName("Добавление социальной ссылки - ссылка уже существует")
    void addSocialLink_AlreadyExists() {
        when(profileRepository.findById(1L)).thenReturn(Optional.of(testProfile));
        when(socialLinkRepository.findByProfileAndPlatform(testProfile, SocialPlatform.VK))
                .thenReturn(Optional.of(testSocialLink));

        assertThatThrownBy(() -> socialLinkService.addSocialLink(1L,
                SocialPlatform.VK, "id"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("уже указана");
    }

    // findByProfile

    @Test
    @DisplayName("Поиск ссылок по профилю - успех")
    void findByProfile_Success() {
        when(profileRepository.findById(1L)).thenReturn(Optional.of(testProfile));
        when(socialLinkRepository.findByProfile(testProfile)).thenReturn(List.of(testSocialLink));

        List<SocialLinkDto> result = socialLinkService.findByProfile(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getPlatform()).isEqualTo(SocialPlatform.VK);
    }

    @Test
    @DisplayName("Поиск ссылок по профилю - профиль не найден")
    void findByProfile_ProfileNotFound() {
        when(profileRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> socialLinkService.findByProfile(999L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Профиль не найден");
    }

    // findPrimaryLinks

    @Test
    @DisplayName("Поиск основных ссылок - успех")
    void findPrimaryLinks_Success() {
        when(profileRepository.findById(1L)).thenReturn(Optional.of(testProfile));
        when(socialLinkRepository.findByProfileAndIsPrimaryTrue(testProfile))
                .thenReturn(List.of(testSocialLink));

        List<SocialLinkDto> result = socialLinkService.findPrimaryLinks(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getIsPrimary()).isTrue();
    }

    // setPrimary

    @Test
    @DisplayName("Установка основной ссылки - успех")
    void setPrimary_Success() {
        SocialLink anotherLink = SocialLink.builder()
                .id(2L)
                .profile(testProfile)
                .platform(SocialPlatform.TELEGRAM)
                .userIdentifier("telegram_id")
                .isPrimary(false)
                .build();

        when(socialLinkRepository.findById(1L)).thenReturn(Optional.of(testSocialLink));
        doNothing().when(socialLinkRepository).resetPrimaryFlag(testProfile);
        when(socialLinkRepository.save(any(SocialLink.class))).thenReturn(testSocialLink);

        socialLinkService.setPrimary(1L);

        verify(socialLinkRepository, times(1)).resetPrimaryFlag(testProfile);
        verify(socialLinkRepository, times(1)).save(testSocialLink);
        assertThat(testSocialLink.getIsPrimary()).isTrue();
    }

    @Test
    @DisplayName("Установка основной ссылки - ссылка не найдена")
    void setPrimary_LinkNotFound() {
        when(socialLinkRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> socialLinkService.setPrimary(999L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Ссылка не найдена");
    }

    // updateUserIdentifier

    @Test
    @DisplayName("Обновление идентификатора пользователя - успех")
    void updateUserIdentifier_Success() {
        when(socialLinkRepository.findById(1L)).thenReturn(Optional.of(testSocialLink));
        when(socialLinkRepository.save(any(SocialLink.class))).thenReturn(testSocialLink);

        socialLinkService.updateUserIdentifier(1L, "new_vk_id");

        verify(socialLinkRepository, times(1)).save(testSocialLink);
        assertThat(testSocialLink.getUserIdentifier()).isEqualTo("new_vk_id");
    }

    // removeSocialLink

    @Test
    @DisplayName("Удаление ссылки - успех")
    void removeSocialLink_Success() {
        when(socialLinkRepository.findById(1L)).thenReturn(Optional.of(testSocialLink));
        doNothing().when(socialLinkRepository).delete(testSocialLink);

        socialLinkService.removeSocialLink(1L);

        verify(socialLinkRepository, times(1)).delete(testSocialLink);
    }

    // removeAllByProfile

    @Test
    @DisplayName("Удаление всех ссылок профиля - успех")
    void removeAllByProfile_Success() {
        when(profileRepository.findById(1L)).thenReturn(Optional.of(testProfile));
        doNothing().when(socialLinkRepository).deleteByProfile(testProfile);

        socialLinkService.removeAllByProfile(1L);

        verify(socialLinkRepository, times(1)).deleteByProfile(testProfile);
    }

    //  getFullUrl

    @Test
    @DisplayName("Получение полного URL - успех")
    void getFullUrl_Success() {
        String result = socialLinkService.getFullUrl(SocialPlatform.VK, "test_user");

        assertThat(result).isEqualTo("https://vk.com/test_user");
    }

    @Test
    @DisplayName("Получение полного URL для Telegram")
    void getFullUrl_Telegram() {
        String result = socialLinkService.getFullUrl(SocialPlatform.TELEGRAM, "test_user");

        assertThat(result).isEqualTo("https://t.me/test_user");
    }
}