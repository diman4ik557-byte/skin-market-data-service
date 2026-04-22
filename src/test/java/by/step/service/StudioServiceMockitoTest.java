package by.step.service;

import by.step.dto.ArtistProfileDto;
import by.step.dto.StudioDto;
import by.step.dto.StudioMemberDto;
import by.step.entity.*;
import by.step.enums.StudioMemberStatus;
import by.step.enums.StudioRole;
import by.step.enums.UserRole;
import by.step.repository.*;
import by.step.service.impl.StudioServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.stubbing.Answer;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Тесты сервиса студий")
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
    private StudioMemberRepository studioMemberRepository;

    @InjectMocks
    private StudioServiceImpl studioService;

    private User testUser;
    private Profile testProfile;
    private ArtistProfile testArtist;
    private Studio testStudio;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .role(UserRole.ARTIST)
                .build();

        testProfile = Profile.builder()
                .id(1L)
                .user(testUser)
                .isArtist(true)
                .isStudio(false)
                .build();

        testArtist = ArtistProfile.builder()
                .id(1L)
                .profile(testProfile)
                .isAvailable(true)
                .build();

        testStudio = Studio.builder()
                .id(1L)
                .profile(testProfile)
                .name("Test Studio")
                .description("Test Description")
                .foundedAt(LocalDate.now())
                .manager(testUser)
                .build();
    }

    // createStudio
    @Test
    @DisplayName("Создание студии - успех")
    void createStudio_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(profileRepository.findByUserId(1L)).thenReturn(Optional.of(testProfile));
        when(studioRepository.findByProfile(testProfile)).thenReturn(Optional.empty());
        when(studioRepository.save(any(Studio.class))).thenReturn(testStudio);
        when(artistProfileRepository.findByProfile(testProfile)).thenReturn(Optional.of(testArtist));
        when(studioMemberRepository.save(any(StudioMember.class))).thenReturn(new StudioMember());

        StudioDto result = studioService.createStudio(1L, "Test Studio", "Test Description");

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Test Studio");
        assertThat(result.getManagerId()).isEqualTo(1L);
        verify(studioRepository, times(1)).save(any(Studio.class));
    }

    @Test
    @DisplayName("Создание студии - пользователь не найден")
    void createStudio_UserNotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> studioService.createStudio(999L, "Name", "Desc"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Пользователь не найден");
    }

    @Test
    @DisplayName("Создание студии - профиль не найден")
    void createStudio_ProfileNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(profileRepository.findByUserId(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> studioService.createStudio(1L, "Name", "Desc"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Профиль пользователя не найден");
    }

    @Test
    @DisplayName("Создание студии - студия уже существует")
    void createStudio_StudioAlreadyExists() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(profileRepository.findByUserId(1L)).thenReturn(Optional.of(testProfile));
        when(studioRepository.findByProfile(testProfile)).thenReturn(Optional.of(testStudio));

        assertThatThrownBy(() -> studioService.createStudio(1L, "Name", "Desc"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Студия уже существует");
    }

    // findById

    @Test
    @DisplayName("Поиск студии по ID - успех")
    void findById_Success() {
        when(studioRepository.findById(1L)).thenReturn(Optional.of(testStudio));

        Optional<StudioDto> result = studioService.findById(1L);

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(1L);
        assertThat(result.get().getName()).isEqualTo("Test Studio");
    }

    @Test
    @DisplayName("Поиск студии по ID - не найдена")
    void findById_NotFound() {
        when(studioRepository.findById(999L)).thenReturn(Optional.empty());

        Optional<StudioDto> result = studioService.findById(999L);

        assertThat(result).isEmpty();
    }

    // findAllStudios

    @Test
    @DisplayName("Получение всех студий")
    void findAllStudios_Success() {
        when(studioRepository.findAll()).thenReturn(List.of(testStudio));

        List<StudioDto> result = studioService.findAllStudios();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Test Studio");
    }

    //  getStudioMembers

    @Test
    @DisplayName("Получение участников студии")
    void getStudioMembers_Success() {
        StudioMember member = StudioMember.builder()
                .studio(testStudio)
                .member(testArtist)
                .status(StudioMemberStatus.APPROVED)
                .build();

        when(studioRepository.findById(1L)).thenReturn(Optional.of(testStudio));
        when(studioMemberRepository.findByStudioAndStatus(testStudio, StudioMemberStatus.APPROVED))
                .thenReturn(List.of(member));

        List<ArtistProfileDto> result = studioService.getStudioMembers(1L);

        assertThat(result).hasSize(1);
    }

    // requestToJoinStudio

    @Test
    @DisplayName("Подача заявки на вступление - успех")
    void requestToJoinStudio_Success() {
        when(studioRepository.findById(1L)).thenReturn(Optional.of(testStudio));
        when(artistProfileRepository.findById(2L)).thenReturn(Optional.of(testArtist));
        when(studioMemberRepository.existsByStudioAndMember(testStudio, testArtist)).thenReturn(false);
        when(studioMemberRepository.save(any(StudioMember.class))).thenReturn(new StudioMember());

        studioService.requestToJoinStudio(1L, 2L);

        verify(studioMemberRepository, times(1)).save(any(StudioMember.class));
    }

    @Test
    @DisplayName("Подача заявки на вступление - художник уже в студии")
    void requestToJoinStudio_ArtistAlreadyInStudio() {
        testArtist.setStudio(testStudio);
        when(studioRepository.findById(1L)).thenReturn(Optional.of(testStudio));
        when(artistProfileRepository.findById(2L)).thenReturn(Optional.of(testArtist));

        assertThatThrownBy(() -> studioService.requestToJoinStudio(1L, 2L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Художник уже состоит в студии");
    }

    // deleteStudio

    @Test
    @DisplayName("Удаление студии - успех (с участниками)")
    void deleteStudio_WithMembers_Success() {
        when(studioRepository.findById(1L)).thenReturn(Optional.of(testStudio));
        when(artistProfileRepository.findByProfileUserId(1L)).thenReturn(Optional.of(testArtist));
        when(studioMemberRepository.findByStudioAndMember(testStudio, testArtist))
                .thenReturn(Optional.of(StudioMember.builder().role(StudioRole.MANAGER).build()));
        when(artistProfileRepository.findByStudio(testStudio)).thenReturn(List.of(testArtist));
        doNothing().when(studioRepository).delete(testStudio);

        studioService.deleteStudio(1L, 1L);

        verify(studioRepository, times(1)).delete(testStudio);
        verify(artistProfileRepository, times(1)).save(testArtist);
    }

    @Test
    @DisplayName("Удаление студии - нет прав")
    void deleteStudio_NotManager() {
        when(studioRepository.findById(1L)).thenReturn(Optional.of(testStudio));
        when(artistProfileRepository.findByProfileUserId(999L)).thenReturn(Optional.of(testArtist));
        when(studioMemberRepository.findByStudioAndMember(testStudio, testArtist))
                .thenReturn(Optional.of(StudioMember.builder().role(StudioRole.ARTIST).build()));

        assertThatThrownBy(() -> studioService.deleteStudio(1L, 999L))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("Только менеджер или администратор");
    }

    // дополнительно

// approveMember
    @Test
    @DisplayName("Одобрение заявки - успех")
    void approveMember_Success() {

        User managerUser = User.builder()
                .id(1L)
                .username("manager")
                .email("manager@example.com")
                .role(UserRole.ARTIST)
                .build();

        Profile managerProfile = Profile.builder()
                .id(1L)
                .user(managerUser)
                .isArtist(true)
                .build();

        ArtistProfile managerArtist = ArtistProfile.builder()
                .id(1L)
                .profile(managerProfile)
                .isAvailable(true)
                .build();

        StudioMember managerMember = StudioMember.builder()
                .id(10L)
                .studio(testStudio)
                .member(managerArtist)
                .role(StudioRole.MANAGER)
                .status(StudioMemberStatus.APPROVED)
                .build();

        User applicantUser = User.builder()
                .id(2L)
                .username("applicant")
                .email("applicant@example.com")
                .role(UserRole.ARTIST)
                .build();

        Profile applicantProfile = Profile.builder()
                .id(2L)
                .user(applicantUser)
                .isArtist(true)
                .build();

        ArtistProfile applicantArtist = ArtistProfile.builder()
                .id(2L)
                .profile(applicantProfile)
                .isAvailable(true)
                .build();

        StudioMember pendingMember = StudioMember.builder()
                .id(1L)
                .studio(testStudio)
                .member(applicantArtist)
                .role(StudioRole.ARTIST)
                .status(StudioMemberStatus.PENDING)
                .build();

        // Моки
        when(studioRepository.findById(1L)).thenReturn(Optional.of(testStudio));
        when(artistProfileRepository.findByProfileUserId(1L)).thenReturn(Optional.of(managerArtist));
        when(studioMemberRepository.findByStudioAndMember(eq(testStudio), any(ArtistProfile.class)))
                .thenReturn(Optional.of(managerMember));
        when(artistProfileRepository.findById(2L)).thenReturn(Optional.of(applicantArtist));
        when(studioMemberRepository.findByStudioAndMember(testStudio, applicantArtist))
                .thenReturn(Optional.of(pendingMember));
        when(studioMemberRepository.save(any(StudioMember.class))).thenAnswer(invocation -> invocation.getArgument(0));

        studioService.approveMember(1L, 2L, 1L);

        verify(studioMemberRepository, times(1)).save(pendingMember);
        verify(artistProfileRepository, times(1)).save(applicantArtist);
        assertThat(pendingMember.getStatus()).isEqualTo(StudioMemberStatus.APPROVED);
        assertThat(applicantArtist.getStudio()).isEqualTo(testStudio);
    }


    @Test
    @DisplayName("Одобрение заявки - студия не найдена")
    void approveMember_StudioNotFound() {
        when(studioRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> studioService.approveMember(999L, 2L, 1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Студия не найдена");
    }

    @Test
    @DisplayName("Одобрение заявки - художник не найден")
    void approveMember_ArtistNotFound() {
        when(studioRepository.findById(1L)).thenReturn(Optional.of(testStudio));
        when(artistProfileRepository.findByProfileUserId(1L)).thenReturn(Optional.of(testArtist));

        when(studioMemberRepository.findByStudioAndMember(eq(testStudio), any(ArtistProfile.class)))
                .thenReturn(Optional.of(StudioMember.builder().role(StudioRole.MANAGER).build()));
        when(artistProfileRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> studioService.approveMember(1L, 999L, 1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Художник не найден");
    }


    @Test
    @DisplayName("Одобрение заявки - нет прав менеджера")
    void approveMember_NotManager() {
        when(studioRepository.findById(1L)).thenReturn(Optional.of(testStudio));
        when(artistProfileRepository.findByProfileUserId(2L)).thenReturn(Optional.of(testArtist));

        when(studioMemberRepository.findByStudioAndMember(eq(testStudio), any(ArtistProfile.class)))
                .thenReturn(Optional.of(StudioMember.builder().role(StudioRole.ARTIST).build()));

        assertThatThrownBy(() -> studioService.approveMember(1L, 2L, 2L))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("Только менеджер или администратор");
    }

//  removeMember

    @Test
    @DisplayName("Удаление участника - успех")
    void removeMember_Success() {
        when(studioRepository.findById(1L)).thenReturn(Optional.of(testStudio));
        when(artistProfileRepository.findById(2L)).thenReturn(Optional.of(testArtist));
        when(artistProfileRepository.findByProfileUserId(1L)).thenReturn(Optional.of(testArtist));
        when(studioMemberRepository.findByStudioAndMember(testStudio, testArtist))
                .thenReturn(Optional.of(StudioMember.builder().role(StudioRole.MANAGER).build()));

        studioService.removeMember(1L, 2L, 1L);

        verify(studioMemberRepository, times(1)).deleteByStudioAndMember(testStudio, testArtist);
        verify(artistProfileRepository, times(1)).save(testArtist);
        assertThat(testArtist.getStudio()).isNull();
    }

    @Test
    @DisplayName("Удаление участника - менеджер удаляет себя")
    void removeMember_ManagerRemovesSelf() {
        assertThatThrownBy(() -> studioService.removeMember(1L, 1L, 1L))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("Менеджер не может удалить сам себя");
    }

// leaveStudio

    @Test
    @DisplayName("Выход из студии - успех")
    void leaveStudio_Success() {
        when(studioRepository.findById(1L)).thenReturn(Optional.of(testStudio));
        when(artistProfileRepository.findById(2L)).thenReturn(Optional.of(testArtist));

        studioService.leaveStudio(1L, 2L);

        verify(studioMemberRepository, times(1)).deleteByStudioAndMember(testStudio, testArtist);
        verify(artistProfileRepository, times(1)).save(testArtist);
        assertThat(testArtist.getStudio()).isNull();
    }

    @Test
    @DisplayName("Выход из студии - студия не найдена")
    void leaveStudio_StudioNotFound() {
        when(studioRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> studioService.leaveStudio(999L, 2L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Студия не найдена");
    }

// isManager

    @Test
    @DisplayName("Проверка является ли пользователь менеджером - да")
    void isManager_True() {
        when(artistProfileRepository.findByProfileUserId(1L)).thenReturn(Optional.of(testArtist));
        when(studioRepository.findById(1L)).thenReturn(Optional.of(testStudio));
        when(studioMemberRepository.findByStudioAndMember(testStudio, testArtist))
                .thenReturn(Optional.of(StudioMember.builder().role(StudioRole.MANAGER).build()));

        boolean result = studioService.isManager(1L, 1L);

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("Проверка является ли пользователь менеджером - нет")
    void isManager_False() {
        when(artistProfileRepository.findByProfileUserId(2L)).thenReturn(Optional.of(testArtist));
        when(studioRepository.findById(1L)).thenReturn(Optional.of(testStudio));
        when(studioMemberRepository.findByStudioAndMember(testStudio, testArtist))
                .thenReturn(Optional.of(StudioMember.builder().role(StudioRole.ARTIST).build()));

        boolean result = studioService.isManager(2L, 1L);

        assertThat(result).isFalse();
    }

// updateDescription

    @Test
    @DisplayName("Обновление описания студии - успех")
    void updateDescription_Success() {
        when(studioRepository.findById(1L)).thenReturn(Optional.of(testStudio));
        when(artistProfileRepository.findByProfileUserId(1L)).thenReturn(Optional.of(testArtist));
        when(studioMemberRepository.findByStudioAndMember(testStudio, testArtist))
                .thenReturn(Optional.of(StudioMember.builder().role(StudioRole.MANAGER).build()));
        when(studioRepository.save(any(Studio.class))).thenReturn(testStudio);

        StudioDto result = studioService.updateDescription(1L, "New Description", 1L);

        assertThat(result).isNotNull();
        verify(studioRepository, times(1)).save(testStudio);
    }

//  getPendingRequests

    @Test
    @DisplayName("Получение ожидающих заявок")
    void getPendingRequests_Success() {
        StudioMember pendingMember = StudioMember.builder()
                .id(1L)
                .studio(testStudio)
                .member(testArtist)
                .status(StudioMemberStatus.PENDING)
                .build();

        when(studioRepository.findById(1L)).thenReturn(Optional.of(testStudio));
        when(artistProfileRepository.findByProfileUserId(1L)).thenReturn(Optional.of(testArtist));
        when(studioMemberRepository.findByStudioAndMember(testStudio, testArtist))
                .thenReturn(Optional.of(StudioMember.builder().role(StudioRole.MANAGER).build()));
        when(studioMemberRepository.findByStudioAndStatus(testStudio, StudioMemberStatus.PENDING))
                .thenReturn(List.of(pendingMember));

        List<StudioMemberDto> result = studioService.getPendingRequests(1L, 1L);

        assertThat(result).hasSize(1);
    }

//  findStudiosByName

    @Test
    @DisplayName("Поиск студий по имени - успех")
    void findStudiosByName_Success() {
        when(studioRepository.findByNameContainingIgnoreCase("Test")).thenReturn(List.of(testStudio));

        List<StudioDto> result = studioService.findStudiosByName("Test");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Test Studio");
    }

    @Test
    @DisplayName("Поиск студий по имени - ничего не найдено")
    void findStudiosByName_NotFound() {
        when(studioRepository.findByNameContainingIgnoreCase("Unknown")).thenReturn(List.of());

        List<StudioDto> result = studioService.findStudiosByName("Unknown");

        assertThat(result).isEmpty();
    }

// getMemberCount

    @Test
    @DisplayName("Получение количества участников студии")
    void getMemberCount_Success() {
        when(studioRepository.findById(1L)).thenReturn(Optional.of(testStudio));
        when(studioMemberRepository.findByStudioAndStatus(testStudio, StudioMemberStatus.APPROVED))
                .thenReturn(List.of(new StudioMember(), new StudioMember()));

        long result = studioService.getMemberCount(1L);

        assertThat(result).isEqualTo(2);
    }

//  findByUserId

    @Test
    @DisplayName("Поиск студии по ID пользователя ")
    void findByUserId_Success() {
        when(studioRepository.findByProfileUserId(1L)).thenReturn(Optional.of(testStudio));

        Optional<StudioDto> result = studioService.findByUserId(1L);

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("Поиск студии по ID пользователя - не найдена")
    void findByUserId_NotFound() {
        when(studioRepository.findByProfileUserId(999L)).thenReturn(Optional.empty());

        Optional<StudioDto> result = studioService.findByUserId(999L);

        assertThat(result).isEmpty();
    }

// findAllStudios(Pageable)

    @Test
    @DisplayName("Получение всех студий с пагинацией")
    void findAllStudiosPageable_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Studio> studioPage = new PageImpl<>(List.of(testStudio), pageable, 1);

        when(studioRepository.findAll(pageable)).thenReturn(studioPage);

        Page<StudioDto> result = studioService.findAllStudios(pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
    }
}