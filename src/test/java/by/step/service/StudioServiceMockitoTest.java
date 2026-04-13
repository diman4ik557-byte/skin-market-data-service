package by.step.service;

import by.step.dto.StudioDto;
import by.step.entity.ArtistProfile;
import by.step.entity.Profile;
import by.step.entity.Studio;
import by.step.entity.User;
import by.step.repository.ArtistProfileRepository;
import by.step.repository.ProfileRepository;
import by.step.repository.StudioMemberRepository;
import by.step.repository.StudioRepository;
import by.step.repository.UserRepository;
import by.step.service.impl.StudioServiceImpl;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StudioServiceMockitoTest {

    @Mock
    private StudioRepository studioRepository;

    @Mock
    private ProfileRepository profileRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ArtistProfileRepository artistProfileRepository;

    @Mock
    private StudioMemberRepository studioMemberRepository;  // Добавляем

    @InjectMocks
    private StudioServiceImpl studioService;

    @Test
    void createStudio_savesEntity() {
        User user = User.builder().id(1L).username("studio_owner").build();
        Profile profile = Profile.builder().id(1L).user(user).isStudio(false).build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(profileRepository.findByUserId(1L)).thenReturn(Optional.of(profile));
        when(studioRepository.findByProfile(profile)).thenReturn(Optional.empty());
        when(profileRepository.save(any(Profile.class))).thenReturn(profile);

        Studio studio = Studio.builder()
                .id(1L)
                .profile(profile)
                .name("Cool Studio")
                .description("Описание")
                .foundedAt(LocalDate.now())
                .manager(user)
                .build();

        when(studioRepository.save(any(Studio.class))).thenReturn(studio);

        StudioDto result = studioService.createStudio(1L, "Cool Studio", "Описание");

        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result.getName()).isEqualTo("Cool Studio");
        verify(studioRepository, times(1)).save(any(Studio.class));
    }

    @Test
    void createStudio_throwsWhenUserNotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        Assertions.assertThatThrownBy(() -> studioService.createStudio(999L, "Name", "Desc"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Пользователь не найден - 999");

        verify(studioRepository, never()).save(any(Studio.class));
    }

    @Test
    void findById_returnsStudio() {
        Profile profile = Profile.builder().id(1L).build();
        User manager = User.builder().id(1L).username("manager").build();
        Studio studio = Studio.builder()
                .id(1L)
                .profile(profile)
                .name("Cool Studio")
                .manager(manager)
                .build();

        when(studioRepository.findById(1L)).thenReturn(Optional.of(studio));

        Optional<StudioDto> result = studioService.findById(1L);

        Assertions.assertThat(result).isPresent();
        Assertions.assertThat(result.get().getName()).isEqualTo("Cool Studio");
    }

    @Test
    void findAllStudios_returnsList() {
        Profile profile = Profile.builder().id(1L).build();
        Studio studio = Studio.builder().id(1L).profile(profile).name("Studio 1").build();

        when(studioRepository.findAll()).thenReturn(List.of(studio));

        List<StudioDto> result = studioService.findAllStudios();

        Assertions.assertThat(result).hasSize(1);
        verify(studioRepository).findAll();
    }

    @Test
    void findStudiosByName_returnsList() {
        Profile profile = Profile.builder().id(1L).build();
        Studio studio = Studio.builder().id(1L).profile(profile).name("Cool Studio").build();

        when(studioRepository.findByNameContainingIgnoreCase("Cool")).thenReturn(List.of(studio));


        List<StudioDto> result = studioService.findStudiosByName("Cool");

        Assertions.assertThat(result).hasSize(1);
        verify(studioRepository).findByNameContainingIgnoreCase("Cool");
    }

    @Test
    void updateDescription_updatesDescription() {
        Profile profile = Profile.builder().id(1L).build();
        Studio studio = Studio.builder()
                .id(1L)
                .profile(profile)
                .name("Studio")
                .description("Старое описание")
                .build();

        when(studioRepository.findById(1L)).thenReturn(Optional.of(studio));
        when(studioRepository.save(any(Studio.class))).thenReturn(studio);

        StudioDto result = studioService.updateDescription(1L, "Новое описание");

        Assertions.assertThat(result.getDescription()).isEqualTo("Новое описание");
        verify(studioRepository).save(studio);
    }

    @Test
    void addMember_addsArtistToStudio() {
        Studio studio = Studio.builder().id(1L).name("Studio").build();
        ArtistProfile artist = ArtistProfile.builder().id(1L).build();

        when(studioRepository.findById(1L)).thenReturn(Optional.of(studio));
        when(artistProfileRepository.findById(1L)).thenReturn(Optional.of(artist));
        when(studioMemberRepository.existsByStudioAndMember(studio, artist)).thenReturn(false);
        when(artistProfileRepository.save(any(ArtistProfile.class))).thenReturn(artist);

        studioService.addMember(1L, 1L, "ARTIST");

        verify(artistProfileRepository).save(artist);
        Assertions.assertThat(artist.getStudio()).isEqualTo(studio);
    }

    @Test
    void addMember_throwsWhenArtistAlreadyInStudio() {
        Studio studio = Studio.builder().id(1L).name("Studio").build();
        ArtistProfile artist = ArtistProfile.builder().id(1L).studio(studio).build();

        when(studioRepository.findById(1L)).thenReturn(Optional.of(studio));
        when(artistProfileRepository.findById(1L)).thenReturn(Optional.of(artist));
        when(studioMemberRepository.existsByStudioAndMember(studio, artist)).thenReturn(true);

        Assertions.assertThatThrownBy(() -> studioService.addMember(1L, 1L, "ARTIST"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Художник уже в студии");

        verify(artistProfileRepository, never()).save(artist);
    }

    @Test
    void removeMember_removesArtistFromStudio() {
        Studio studio = Studio.builder().id(1L).name("Studio").build();
        ArtistProfile artist = ArtistProfile.builder().id(1L).studio(studio).build();

        when(studioRepository.findById(1L)).thenReturn(Optional.of(studio));
        when(artistProfileRepository.findById(1L)).thenReturn(Optional.of(artist));
        when(artistProfileRepository.save(any(ArtistProfile.class))).thenReturn(artist);

        studioService.removeMember(1L, 1L);

        verify(artistProfileRepository).save(artist);
        Assertions.assertThat(artist.getStudio()).isNull();
    }

    @Test
    void getStudioMembers_returnsList() {
        Studio studio = Studio.builder().id(1L).name("Studio").build();
        ArtistProfile artist = ArtistProfile.builder().id(1L).build();

        when(studioRepository.findById(1L)).thenReturn(Optional.of(studio));
        when(artistProfileRepository.findByStudio(studio)).thenReturn(List.of(artist));

        var result = studioService.getStudioMembers(1L);

        Assertions.assertThat(result).hasSize(1);
        verify(artistProfileRepository).findByStudio(studio);
    }

    @Test
    void getMemberCount_returnsCount() {
        Studio studio = Studio.builder().id(1L).name("Studio").build();
        List<ArtistProfile> members = List.of(
                ArtistProfile.builder().id(1L).build(),
                ArtistProfile.builder().id(2L).build()
        );

        when(studioRepository.findById(1L)).thenReturn(Optional.of(studio));
        when(artistProfileRepository.findByStudio(studio)).thenReturn(members);

        long result = studioService.getMemberCount(1L);

        Assertions.assertThat(result).isEqualTo(2);
    }

    @Test
    void deleteStudio_deletesStudioAndDetachesMembers() {
        Studio studio = Studio.builder().id(1L).name("Studio").build();
        ArtistProfile member1 = ArtistProfile.builder().id(1L).studio(studio).build();
        ArtistProfile member2 = ArtistProfile.builder().id(2L).studio(studio).build();
        List<ArtistProfile> members = List.of(member1, member2);

        when(studioRepository.findById(1L)).thenReturn(Optional.of(studio));
        when(artistProfileRepository.findByStudio(studio)).thenReturn(members);
        doNothing().when(studioRepository).delete(studio);

        studioService.deleteStudio(1L);

        verify(artistProfileRepository, times(2)).save(any(ArtistProfile.class));
        verify(studioRepository).delete(studio);
    }
}