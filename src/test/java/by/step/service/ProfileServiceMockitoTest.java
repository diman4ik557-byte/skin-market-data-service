package by.step.service;

import by.step.dto.ProfileDto;
import by.step.entity.Profile;
import by.step.entity.User;
import by.step.enums.UserRole;
import by.step.repository.ProfileRepository;
import by.step.repository.UserRepository;
import by.step.service.impl.ProfileServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Тесты сервиса профилей")
class ProfileServiceMockitoTest {

    @Mock
    private ProfileRepository profileRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ProfileServiceImpl profileService;

    private User testUser;
    private Profile testProfile;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .password("encodedPassword")
                .role(UserRole.USER)
                .balance(BigDecimal.ZERO)
                .build();

        testProfile = Profile.builder()
                .id(1L)
                .user(testUser)
                .bio("Test bio")
                .isArtist(false)
                .isStudio(false)
                .build();
    }

    // createProfile

    @Test
    @DisplayName("Создание профиля - успех")
    void createProfile_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(profileRepository.findByUserId(1L)).thenReturn(Optional.empty());
        when(profileRepository.save(any(Profile.class))).thenReturn(testProfile);

        ProfileDto result = profileService.createProfile(1L);

        assertThat(result).isNotNull();
        assertThat(result.getUserId()).isEqualTo(1L);
        assertThat(result.getUsername()).isEqualTo("testuser");
        verify(profileRepository, times(1)).save(any(Profile.class));
    }

    @Test
    @DisplayName("Создание профиля - пользователь не найден")
    void createProfile_UserNotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> profileService.createProfile(999L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Пользователь не найден");
    }

    @Test
    @DisplayName("Создание профиля - профиль уже существует")
    void createProfile_ProfileAlreadyExists() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(profileRepository.findByUserId(1L)).thenReturn(Optional.of(testProfile));

        assertThatThrownBy(() -> profileService.createProfile(1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Профиль пользователя уже существует");
    }

    // findByUserId

    @Test
    @DisplayName("Поиск профиля по ID пользователя - успех")
    void findByUserId_Success() {
        when(profileRepository.findByUserId(1L)).thenReturn(Optional.of(testProfile));

        Optional<ProfileDto> result = profileService.findByUserId(1L);

        assertThat(result).isPresent();
        assertThat(result.get().getUserId()).isEqualTo(1L);
        assertThat(result.get().getUsername()).isEqualTo("testuser");
        assertThat(result.get().getBio()).isEqualTo("Test bio");
    }

    @Test
    @DisplayName("Поиск профиля по ID пользователя - не найден")
    void findByUserId_NotFound() {
        when(profileRepository.findByUserId(999L)).thenReturn(Optional.empty());

        Optional<ProfileDto> result = profileService.findByUserId(999L);

        assertThat(result).isEmpty();
    }

    // updateBio

    @Test
    @DisplayName("Обновление био профиля - успех")
    void updateBio_Success() {
        Profile updatedProfile = Profile.builder()
                .id(1L)
                .user(testUser)
                .bio("New bio content")
                .build();

        when(profileRepository.findById(1L)).thenReturn(Optional.of(testProfile));
        when(profileRepository.save(any(Profile.class))).thenReturn(updatedProfile);

        ProfileDto result = profileService.updateBio(1L, "New bio content");

        assertThat(result).isNotNull();
        assertThat(result.getBio()).isEqualTo("New bio content");
        verify(profileRepository, times(1)).save(any(Profile.class));
    }

    @Test
    @DisplayName("Обновление био профиля - профиль не найден")
    void updateBio_ProfileNotFound() {
        when(profileRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> profileService.updateBio(999L, "New bio"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Профиль не найден");
    }

    // setArtistStatus

    @Test
    @DisplayName("Установка статуса художника - успех")
    void setArtistStatus_Success() {
        when(profileRepository.findById(1L)).thenReturn(Optional.of(testProfile));
        when(profileRepository.save(any(Profile.class))).thenReturn(testProfile);

        profileService.setArtistStatus(1L, true);

        verify(profileRepository, times(1)).save(testProfile);
        assertThat(testProfile.getIsArtist()).isTrue();
    }

    @Test
    @DisplayName("Установка статуса художника - профиль не найден")
    void setArtistStatus_ProfileNotFound() {
        when(profileRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> profileService.setArtistStatus(999L, true))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Профиль не найден");
    }

    // setStudioStatus

    @Test
    @DisplayName("Установка статуса студии - успех")
    void setStudioStatus_Success() {
        when(profileRepository.findById(1L)).thenReturn(Optional.of(testProfile));
        when(profileRepository.save(any(Profile.class))).thenReturn(testProfile);

        profileService.setStudioStatus(1L, true);

        verify(profileRepository, times(1)).save(testProfile);
        assertThat(testProfile.getIsStudio()).isTrue();
    }

    // isArtist

    @Test
    @DisplayName("Проверка является ли пользователь художником - да")
    void isArtist_True() {
        testProfile.setIsArtist(true);
        when(profileRepository.findByUserId(1L)).thenReturn(Optional.of(testProfile));

        boolean result = profileService.isArtist(1L);

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("Проверка является ли пользователь художником - нет")
    void isArtist_False() {
        testProfile.setIsArtist(false);
        when(profileRepository.findByUserId(1L)).thenReturn(Optional.of(testProfile));

        boolean result = profileService.isArtist(1L);

        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("Проверка является ли пользователь художником - профиль не найден")
    void isArtist_ProfileNotFound() {
        when(profileRepository.findByUserId(999L)).thenReturn(Optional.empty());

        boolean result = profileService.isArtist(999L);

        assertThat(result).isFalse();
    }

    //  isStudio

    @Test
    @DisplayName("Проверка является ли пользователь студией - да")
    void isStudio_True() {
        testProfile.setIsStudio(true);
        when(profileRepository.findByUserId(1L)).thenReturn(Optional.of(testProfile));

        boolean result = profileService.isStudio(1L);

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("Проверка является ли пользователь студией - нет")
    void isStudio_False() {
        testProfile.setIsStudio(false);
        when(profileRepository.findByUserId(1L)).thenReturn(Optional.of(testProfile));

        boolean result = profileService.isStudio(1L);

        assertThat(result).isFalse();
    }
}