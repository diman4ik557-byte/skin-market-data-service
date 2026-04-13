package by.step.service;
import by.step.dto.ProfileDto;
import by.step.entity.Profile;
import by.step.entity.User;
import by.step.entity.enums.UserRole;
import by.step.repository.ProfileRepository;
import by.step.repository.UserRepository;
import by.step.service.ProfileService;
import by.step.service.impl.ProfileServiceImpl;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProfileServiceMockitoTest {

    @Mock
    private ProfileRepository profileRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ProfileServiceImpl profileService;

    @Test
    void createProfile_savesEntity() {
        User user = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .password("password123")
                .role(UserRole.USER)
                .balance(BigDecimal.ZERO)
                .build();

        Profile profile = Profile.builder()
                .id(1L)
                .user(user)
                .bio("")
                .isArtist(false)
                .isStudio(false)
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(profileRepository.findByUserId(1L)).thenReturn(Optional.empty());
        when(profileRepository.save(any(Profile.class))).thenReturn(profile);

        ProfileDto result = profileService.createProfile(1L);

        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result.getUserId()).isEqualTo(1L);
        Assertions.assertThat(result.getUsername()).isEqualTo("testuser");
        verify(profileRepository, times(1)).save(any(Profile.class));
    }

    @Test
    void createProfile_throwsWhenUserNotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        Assertions.assertThatThrownBy(() -> profileService.createProfile(999L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Пользователь не найден");

        verify(profileRepository, never()).save(any(Profile.class));
    }

    @Test
    void createProfile_throwsWhenProfileExists() {
        User user = User.builder().id(1L).build();
        Profile existingProfile = Profile.builder().id(1L).user(user).build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(profileRepository.findByUserId(1L)).thenReturn(Optional.of(existingProfile));

        Assertions.assertThatThrownBy(() -> profileService.createProfile(1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Профиль пользователя уже сущетсвует - 1");

        verify(profileRepository, never()).save(any(Profile.class));
    }

    @Test
    void findByUserId_returnsProfile() {
        User user = User.builder().id(1L).username("testuser").build();
        Profile profile = Profile.builder().id(1L).user(user).bio("Тестовое био").build();

        when(profileRepository.findByUserId(1L)).thenReturn(Optional.of(profile));

        Optional<ProfileDto> result = profileService.findByUserId(1L);

        Assertions.assertThat(result).isPresent();
        Assertions.assertThat(result.get().getUserId()).isEqualTo(1L);
        Assertions.assertThat(result.get().getUsername()).isEqualTo("testuser");
        Assertions.assertThat(result.get().getBio()).isEqualTo("Тестовое био");
    }

    @Test
    void findByUserId_returnsEmptyWhenNotFound() {
        when(profileRepository.findByUserId(999L)).thenReturn(Optional.empty());

        Optional<ProfileDto> result = profileService.findByUserId(999L);

        Assertions.assertThat(result).isEmpty();
    }

    @Test
    void updateBio_updatesBio() {
        User user = User.builder().id(1L).username("testuser").build();
        Profile profile = Profile.builder().id(1L).user(user).bio("Старое био").build();
        Profile updatedProfile = Profile.builder().id(1L).user(user).bio("Новое био").build();

        when(profileRepository.findById(1L)).thenReturn(Optional.of(profile));
        when(profileRepository.save(any(Profile.class))).thenReturn(updatedProfile);

        ProfileDto result = profileService.updateBio(1L, "Новое био");

        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result.getBio()).isEqualTo("Новое био");
        verify(profileRepository).save(profile);
    }

    @Test
    void updateBio_throwsWhenProfileNotFound() {
        when(profileRepository.findById(999L)).thenReturn(Optional.empty());

        Assertions.assertThatThrownBy(() -> profileService.updateBio(999L, "Новое био"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("не найден");
    }

    @Test
    void setArtistStatus_updatesStatus() {
        User user = User.builder().id(1L).build();
        Profile profile = Profile.builder().id(1L).user(user).isArtist(false).build();

        when(profileRepository.findById(1L)).thenReturn(Optional.of(profile));
        when(profileRepository.save(any(Profile.class))).thenReturn(profile);

        profileService.setArtistStatus(1L, true);

        verify(profileRepository).save(profile);
        Assertions.assertThat(profile.getIsArtist()).isTrue();
    }

    @Test
    void setStudioStatus_updatesStatus() {
        User user = User.builder().id(1L).build();
        Profile profile = Profile.builder().id(1L).user(user).isStudio(false).build();

        when(profileRepository.findById(1L)).thenReturn(Optional.of(profile));
        when(profileRepository.save(any(Profile.class))).thenReturn(profile);

        profileService.setStudioStatus(1L, true);

        verify(profileRepository).save(profile);
        Assertions.assertThat(profile.getIsStudio()).isTrue();
    }

    @Test
    void isArtist_returnsTrue() {
        Profile profile = Profile.builder().id(1L).isArtist(true).build();
        when(profileRepository.findByUserId(1L)).thenReturn(Optional.of(profile));

        boolean result = profileService.isArtist(1L);

        Assertions.assertThat(result).isTrue();
    }

    @Test
    void isArtist_returnsFalseWhenProfileNotFound() {
        when(profileRepository.findByUserId(999L)).thenReturn(Optional.empty());

        boolean result = profileService.isArtist(999L);

        Assertions.assertThat(result).isFalse();
    }

    @Test
    void isStudio_returnsTrue() {
        Profile profile = Profile.builder().id(1L).isStudio(true).build();
        when(profileRepository.findByUserId(1L)).thenReturn(Optional.of(profile));

        boolean result = profileService.isStudio(1L);

        Assertions.assertThat(result).isTrue();
    }
}