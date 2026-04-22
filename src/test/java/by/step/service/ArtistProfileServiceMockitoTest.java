package by.step.service;

import by.step.dto.ArtistProfileDto;
import by.step.entity.ArtistProfile;
import by.step.entity.Profile;
import by.step.entity.Studio;
import by.step.entity.User;
import by.step.enums.UserRole;
import by.step.repository.ArtistProfileRepository;
import by.step.repository.ProfileRepository;
import by.step.repository.StudioRepository;
import by.step.repository.UserRepository;
import by.step.service.impl.ArtistProfileServiceImpl;
import org.assertj.core.api.Assertions;
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
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ArtistProfileServiceMockitoTest {

    @Mock
    private ArtistProfileRepository artistProfileRepository;

    @Mock
    private ProfileRepository profileRepository;

    @Mock
    private StudioRepository studioRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ArtistProfileServiceImpl artistProfileService;

    private User testUser;
    private Profile testProfile;
    private ArtistProfile testArtist;
    private Studio testStudio;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .username("testartist")
                .email("artist@example.com")
                .role(UserRole.ARTIST)
                .build();

        testProfile = Profile.builder()
                .id(1L)
                .user(testUser)
                .isArtist(true)
                .build();

        testArtist = ArtistProfile.builder()
                .id(1L)
                .profile(testProfile)
                .styles("classic, realism")
                .minPrice(BigDecimal.valueOf(1000))
                .averageTime(3)
                .isAvailable(true)
                .build();

        testStudio = Studio.builder()
                .id(1L)
                .name("Test Studio")
                .build();
    }

    @Test
    void createArtistProfile_savesEntity() {
        User user = User.builder().id(1L).username("artist").build();
        Profile profile = Profile.builder().id(1L).user(user).isArtist(false).build();

        when(profileRepository.findByUserId(1L)).thenReturn(Optional.of(profile));
        when(artistProfileRepository.findByProfile(profile)).thenReturn(Optional.empty());
        when(profileRepository.save(any(Profile.class))).thenReturn(profile);

        ArtistProfile artistProfile = ArtistProfile.builder()
                .id(1L)
                .profile(profile)
                .styles("классический, реализм")
                .minPrice(BigDecimal.valueOf(1000))
                .averageTime(3)
                .isAvailable(true)
                .build();

        when(artistProfileRepository.save(any(ArtistProfile.class))).thenReturn(artistProfile);

        ArtistProfileDto result = artistProfileService.createArtistProfile(1L, "классический, реализм",
                BigDecimal.valueOf(1000), 3);

        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result.getStyles()).isEqualTo("классический, реализм");
        verify(artistProfileRepository, times(1)).save(any(ArtistProfile.class));
    }

    @Test
    void createArtistProfile_throwsWhenProfileNotFound() {
        when(profileRepository.findByUserId(999L)).thenReturn(Optional.empty());

        Assertions.assertThatThrownBy(() -> artistProfileService.createArtistProfile(999L, "стиль",
                        BigDecimal.valueOf(1000), 3))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Профиль пользователя не найден - 999");

        verify(artistProfileRepository, never()).save(any(ArtistProfile.class));
    }

    @Test
    @DisplayName("Создание профиля художника - профиль не найден")
    void createArtistProfile_ProfileNotFound() {
        when(profileRepository.findByUserId(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> artistProfileService.createArtistProfile(999L, "styles", BigDecimal.valueOf(1000), 3))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Профиль пользователя не найден");
    }

    @Test
    void findByUserId_returnsArtistProfile() {
        User user = User.builder().id(1L).username("artist").build();
        Profile profile = Profile.builder().id(1L).user(user).build();
        ArtistProfile artistProfile = ArtistProfile.builder()
                .id(1L)
                .profile(profile)
                .styles("классический")
                .minPrice(BigDecimal.valueOf(1000))
                .build();

        when(artistProfileRepository.findByProfileUserId(1L)).thenReturn(Optional.of(artistProfile));

        Optional<ArtistProfileDto> result = artistProfileService.findByUserId(1L);

        Assertions.assertThat(result).isPresent();
        Assertions.assertThat(result.get().getStyles()).isEqualTo("классический");
    }

    @Test
    void findAllArtists_returnsList() {
        User user = User.builder().id(1L).username("artist1").build();
        Profile profile = Profile.builder().id(1L).user(user).build();
        ArtistProfile artist = ArtistProfile.builder().id(1L).profile(profile).build();

        when(artistProfileRepository.findAll()).thenReturn(List.of(artist));

        List<ArtistProfileDto> result = artistProfileService.findAllArtists();

        Assertions.assertThat(result).hasSize(1);
        verify(artistProfileRepository).findAll();
    }

    @Test
    void findAvailableArtists_returnsList() {
        User user = User.builder().id(1L).username("artist1").build();
        Profile profile = Profile.builder().id(1L).user(user).build();
        ArtistProfile artist = ArtistProfile.builder().id(1L).profile(profile).isAvailable(true).build();

        when(artistProfileRepository.findByIsAvailableTrue()).thenReturn(List.of(artist));

        List<ArtistProfileDto> result = artistProfileService.findAvailableArtists();

        Assertions.assertThat(result).hasSize(1);
        verify(artistProfileRepository).findByIsAvailableTrue();
    }

    @Test
    void findArtistsByStyle_returnsList() {
        User user = User.builder().id(1L).username("artist1").build();
        Profile profile = Profile.builder().id(1L).user(user).build();
        ArtistProfile artist = ArtistProfile.builder().id(1L).profile(profile).styles("классический").build();

        when(artistProfileRepository.findByStylesContaining("классический")).thenReturn(List.of(artist));

        List<ArtistProfileDto> result = artistProfileService.findArtistsByStyle("классический");

        Assertions.assertThat(result).hasSize(1);
        verify(artistProfileRepository).findByStylesContaining("классический");
    }

    @Test
    void findArtistsByMaxPrice_returnsList() {
        User user = User.builder().id(1L).username("artist1").build();
        Profile profile = Profile.builder().id(1L).user(user).build();
        ArtistProfile artist = ArtistProfile.builder().id(1L).profile(profile).minPrice(BigDecimal.valueOf(800)).build();

        when(artistProfileRepository.findByMinPriceLessThanEqual(BigDecimal.valueOf(1000))).thenReturn(List.of(artist));

        List<ArtistProfileDto> result = artistProfileService.findArtistsByMaxPrice(BigDecimal.valueOf(1000));

        Assertions.assertThat(result).hasSize(1);
        verify(artistProfileRepository).findByMinPriceLessThanEqual(BigDecimal.valueOf(1000));
    }

    @Test
    void updateStyles_updatesStyles() {
        User user = User.builder().id(1L).username("artist").build();
        Profile profile = Profile.builder().id(1L).user(user).build();
        ArtistProfile artist = ArtistProfile.builder().id(1L).profile(profile).styles("старый стиль").build();

        when(artistProfileRepository.findById(1L)).thenReturn(Optional.of(artist));
        when(artistProfileRepository.save(any(ArtistProfile.class))).thenReturn(artist);

        ArtistProfileDto result = artistProfileService.updateStyles(1L, "новый стиль");

        Assertions.assertThat(result.getStyles()).isEqualTo("новый стиль");
        verify(artistProfileRepository).save(artist);
    }

    @Test
    void updateMinPrice_updatesPrice() {
        User user = User.builder().id(1L).username("artist").build();
        Profile profile = Profile.builder().id(1L).user(user).build();
        ArtistProfile artist = ArtistProfile.builder().id(1L).profile(profile).minPrice(BigDecimal.valueOf(1000)).build();

        when(artistProfileRepository.findById(1L)).thenReturn(Optional.of(artist));
        when(artistProfileRepository.save(any(ArtistProfile.class))).thenReturn(artist);

        ArtistProfileDto result = artistProfileService.updateMinPrice(1L, BigDecimal.valueOf(2000));

        Assertions.assertThat(result.getMinPrice()).isEqualByComparingTo(BigDecimal.valueOf(2000));
        verify(artistProfileRepository).save(artist);
    }

    @Test
    void updateAvailability_delegatesToRepository() {
        artistProfileService.updateAvailability(1L, false);
        verify(artistProfileRepository).updateAvailability(1L, false);
    }

    @Test
    void assignToStudio_assignsArtistToStudio() {
        User user = User.builder().id(1L).username("artist").build();
        Profile profile = Profile.builder().id(1L).user(user).build();
        ArtistProfile artist = ArtistProfile.builder().id(1L).profile(profile).build();
        Studio studio = Studio.builder().id(1L).name("Test Studio").build();

        when(artistProfileRepository.findById(1L)).thenReturn(Optional.of(artist));
        when(studioRepository.findById(1L)).thenReturn(Optional.of(studio));
        when(artistProfileRepository.save(any(ArtistProfile.class))).thenReturn(artist);

        artistProfileService.assignToStudio(1L, 1L);

        verify(artistProfileRepository).save(artist);
        Assertions.assertThat(artist.getStudio()).isEqualTo(studio);
    }
        // updateAverageTime

        @Test
        @DisplayName("Обновление среднего времени выполнения")
        void updateAverageTime_Success() {
            when(artistProfileRepository.findById(1L)).thenReturn(Optional.of(testArtist));
            when(artistProfileRepository.save(any(ArtistProfile.class))).thenReturn(testArtist);

            ArtistProfileDto result = artistProfileService.updateAverageTime(1L, 5);

            assertThat(result).isNotNull();
            assertThat(result.getAverageTime()).isEqualTo(5);
            verify(artistProfileRepository, times(1)).save(testArtist);
        }

        @Test
        @DisplayName("Обновление среднего времени - художник не найден")
        void updateAverageTime_ArtistNotFound() {
            when(artistProfileRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> artistProfileService.updateAverageTime(999L, 5))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Профиль художника не найден");
        }

        // findArtistsByFilters

        @Test
        @DisplayName("Поиск художников по фильтрам - все параметры")
        void findArtistsByFilters_WithAllParams() {
            when(artistProfileRepository.findByFilters("classic", BigDecimal.valueOf(2000), true))
                    .thenReturn(List.of(testArtist));

            List<ArtistProfileDto> result = artistProfileService.findArtistsByFilters(
                    "classic", BigDecimal.valueOf(2000), true);

            assertThat(result).hasSize(1);
            verify(artistProfileRepository, times(1))
                    .findByFilters("classic", BigDecimal.valueOf(2000), true);
        }

        @Test
        @DisplayName("Поиск художников по фильтрам - только стиль")
        void findArtistsByFilters_OnlyStyle() {
            when(artistProfileRepository.findByFilters("classic", null, null))
                    .thenReturn(List.of(testArtist));

            List<ArtistProfileDto> result = artistProfileService.findArtistsByFilters("classic",
                    null, null);

            assertThat(result).hasSize(1);
            verify(artistProfileRepository, times(1))
                    .findByFilters("classic", null, null);
        }

        @Test
        @DisplayName("Поиск художников по фильтрам - только цена")
        void findArtistsByFilters_OnlyMaxPrice() {
            when(artistProfileRepository.findByFilters(null, BigDecimal.valueOf(2000), null))
                    .thenReturn(List.of(testArtist));

            List<ArtistProfileDto> result = artistProfileService.findArtistsByFilters(null, BigDecimal.valueOf(2000), null);

            assertThat(result).hasSize(1);
            verify(artistProfileRepository, times(1))
                    .findByFilters(null, BigDecimal.valueOf(2000), null);
        }

        @Test
        @DisplayName("Поиск художников по фильтрам - только доступность")
        void findArtistsByFilters_OnlyAvailable() {
            when(artistProfileRepository.findByFilters(null, null, true))
                    .thenReturn(List.of(testArtist));

            List<ArtistProfileDto> result = artistProfileService.findArtistsByFilters(null,
                    null, true);

            assertThat(result).hasSize(1);
            verify(artistProfileRepository, times(1))
                    .findByFilters(null, null, true);
        }

        // removeFromStudio

        @Test
        @DisplayName("Удаление художника из студии ")
        void removeFromStudio_Success() {
            doNothing().when(artistProfileRepository).removeFromStudio(1L);

            artistProfileService.removeFromStudio(1L);

            verify(artistProfileRepository, times(1)).removeFromStudio(1L);
        }

        // assignToStudio (дополнительный)
        @Test
        @DisplayName("Назначение художника в студию - художник не найден")
        void assignToStudio_ArtistNotFound() {
            when(artistProfileRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> artistProfileService.assignToStudio(999L,
                    1L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Профиль художника не найден");
        }

        @Test
        @DisplayName("Назначение художника в студию - студия не найдена")
        void assignToStudio_StudioNotFound() {
            when(artistProfileRepository.findById(1L)).thenReturn(Optional.of(testArtist));
            when(studioRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> artistProfileService.assignToStudio(1L,
                    999L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Студия не найдена");
        }

        // updateStyles (дополнительный)

        @Test
        @DisplayName("Обновление стилей - художник не найден")
        void updateStyles_ArtistNotFound() {
            when(artistProfileRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> artistProfileService.updateStyles(999L,
                    "new style"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Профиль художника не найден");
        }

        // updateMinPrice (дополнительный)

        @Test
        @DisplayName("Обновление минимальной цены - художник не найден")
        void updateMinPrice_ArtistNotFound() {
            when(artistProfileRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> artistProfileService.updateMinPrice(999L,
                    BigDecimal.valueOf(2000)))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Профиль художника не найден");
        }
    }
