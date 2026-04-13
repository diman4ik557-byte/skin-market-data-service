package by.step.service;

import by.step.dto.ArtistProfileDto;
import by.step.entity.ArtistProfile;
import by.step.entity.Profile;
import by.step.entity.Studio;
import by.step.entity.User;
import by.step.repository.ArtistProfileRepository;
import by.step.repository.ProfileRepository;
import by.step.repository.StudioRepository;
import by.step.repository.UserRepository;
import by.step.service.impl.ArtistProfileServiceImpl;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

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
}
