package by.step.service;

import by.step.dto.StudioMemberDto;
import by.step.entity.*;
import by.step.enums.StudioRole;
import by.step.enums.UserRole;
import by.step.repository.ArtistProfileRepository;
import by.step.repository.StudioMemberRepository;
import by.step.repository.StudioRepository;
import by.step.service.impl.StudioMemberServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Тесты сервиса участников студии")
class StudioMemberServiceMockitoTest {

    @Mock
    private StudioMemberRepository studioMemberRepository;
    @Mock
    private StudioRepository studioRepository;
    @Mock
    private ArtistProfileRepository artistProfileRepository;

    @InjectMocks
    private StudioMemberServiceImpl studioMemberService;

    private User testUser;
    private Profile testProfile;
    private ArtistProfile testArtist;
    private Studio testStudio;
    private StudioMember testMember;

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
                .isArtist(true)
                .build();

        testArtist = ArtistProfile.builder()
                .id(1L)
                .profile(testProfile)
                .isAvailable(true)
                .build();

        testStudio = Studio.builder()
                .id(1L)
                .name("Test Studio")
                .description("Test Description")
                .build();

        testMember = StudioMember.builder()
                .id(1L)
                .studio(testStudio)
                .member(testArtist)
                .role(StudioRole.ARTIST)
                .joinedAt(LocalDateTime.now())
                .build();
    }

    // addMember

    @Test
    @DisplayName("Добавление участника в студию - успех")
    void addMember_Success() {
        when(studioRepository.findById(1L)).thenReturn(Optional.of(testStudio));
        when(artistProfileRepository.findById(1L)).thenReturn(Optional.of(testArtist));
        when(studioMemberRepository.existsByStudioAndMember(testStudio, testArtist)).thenReturn(false);
        when(studioMemberRepository.save(any(StudioMember.class))).thenReturn(testMember);

        StudioMemberDto result = studioMemberService.addMember(1L, 1L, StudioRole.ARTIST);

        assertThat(result).isNotNull();
        assertThat(result.getStudioId()).isEqualTo(1L);
        assertThat(result.getArtistId()).isEqualTo(1L);
        verify(studioMemberRepository, times(1)).save(any(StudioMember.class));
    }

    @Test
    @DisplayName("Добавление участника - студия не найдена")
    void addMember_StudioNotFound() {
        when(studioRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> studioMemberService.addMember(999L,
                1L, StudioRole.ARTIST))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Студия не найдена");
    }

    @Test
    @DisplayName("Добавление участника - художник не найден")
    void addMember_ArtistNotFound() {
        when(studioRepository.findById(1L)).thenReturn(Optional.of(testStudio));
        when(artistProfileRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> studioMemberService.addMember(1L,
                999L, StudioRole.ARTIST))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Художник не найден");
    }

    @Test
    @DisplayName("Добавление участника - художник уже в студии")
    void addMember_AlreadyMember() {
        when(studioRepository.findById(1L)).thenReturn(Optional.of(testStudio));
        when(artistProfileRepository.findById(1L)).thenReturn(Optional.of(testArtist));
        when(studioMemberRepository.existsByStudioAndMember(testStudio, testArtist)).thenReturn(true);

        assertThatThrownBy(() -> studioMemberService.addMember(1L, 1L, StudioRole.ARTIST))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("уже в студии");
    }

    // findByStudioAndArtist

    @Test
    @DisplayName("Поиск участника по студии и художнику - успех")
    void findByStudioAndArtist_Success() {
        when(studioRepository.findById(1L)).thenReturn(Optional.of(testStudio));
        when(artistProfileRepository.findById(1L)).thenReturn(Optional.of(testArtist));
        when(studioMemberRepository.findByStudioAndMember(testStudio, testArtist))
                .thenReturn(Optional.of(testMember));

        Optional<StudioMemberDto> result = studioMemberService.findByStudioAndArtist(1L, 1L);

        assertThat(result).isPresent();
        assertThat(result.get().getStudioId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("Поиск участника - не найден")
    void findByStudioAndArtist_NotFound() {
        when(studioRepository.findById(1L)).thenReturn(Optional.of(testStudio));
        when(artistProfileRepository.findById(1L)).thenReturn(Optional.of(testArtist));
        when(studioMemberRepository.findByStudioAndMember(testStudio, testArtist))
                .thenReturn(Optional.empty());

        Optional<StudioMemberDto> result = studioMemberService.findByStudioAndArtist(1L, 1L);

        assertThat(result).isEmpty();
    }

    // findByStudio

    @Test
    @DisplayName("Поиск всех участников студии - успех")
    void findByStudio_Success() {
        when(studioRepository.findById(1L)).thenReturn(Optional.of(testStudio));
        when(studioMemberRepository.findByStudio(testStudio)).thenReturn(List.of(testMember));

        List<StudioMemberDto> result = studioMemberService.findByStudio(1L);

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("Поиск участников студии - студия не найдена")
    void findByStudio_StudioNotFound() {
        when(studioRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> studioMemberService.findByStudio(999L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Студия не найдена");
    }

    // findByArtist

    @Test
    @DisplayName("Поиск студий художника - успех")
    void findByArtist_Success() {
        when(artistProfileRepository.findById(1L)).thenReturn(Optional.of(testArtist));
        when(studioMemberRepository.findByMember(testArtist)).thenReturn(List.of(testMember));

        List<StudioMemberDto> result = studioMemberService.findByArtist(1L);

        assertThat(result).hasSize(1);
    }

    // updateRole

    @Test
    @DisplayName("Обновление роли участника - успех")
    void updateRole_Success() {
        when(studioMemberRepository.findById(1L)).thenReturn(Optional.of(testMember));
        when(studioMemberRepository.save(any(StudioMember.class))).thenReturn(testMember);

        studioMemberService.updateRole(1L, StudioRole.MANAGER);

        verify(studioMemberRepository, times(1)).save(testMember);
        assertThat(testMember.getRole()).isEqualTo(StudioRole.MANAGER);
    }

    @Test
    @DisplayName("Обновление роли - участник не найден")
    void updateRole_MemberNotFound() {
        when(studioMemberRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> studioMemberService.updateRole(999L,
                StudioRole.MANAGER))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Участник студии не найден");
    }

    // removeMember

    @Test
    @DisplayName("Удаление участника - успех")
    void removeMember_Success() {
        when(studioRepository.findById(1L)).thenReturn(Optional.of(testStudio));
        when(artistProfileRepository.findById(1L)).thenReturn(Optional.of(testArtist));
        doNothing().when(studioMemberRepository).deleteByStudioAndMember(testStudio, testArtist);

        studioMemberService.removeMember(1L, 1L);

        verify(studioMemberRepository, times(1)).deleteByStudioAndMember(testStudio, testArtist);
    }

    // isMember

    @Test
    @DisplayName("Проверка является ли пользователь участником - да")
    void isMember_True() {
        when(studioRepository.findById(1L)).thenReturn(Optional.of(testStudio));
        when(artistProfileRepository.findById(1L)).thenReturn(Optional.of(testArtist));
        when(studioMemberRepository.existsByStudioAndMember(testStudio, testArtist)).thenReturn(true);

        boolean result = studioMemberService.isMember(1L, 1L);

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("Проверка является ли пользователь участником - нет")
    void isMember_False() {
        when(studioRepository.findById(1L)).thenReturn(Optional.of(testStudio));
        when(artistProfileRepository.findById(1L)).thenReturn(Optional.of(testArtist));
        when(studioMemberRepository.existsByStudioAndMember(testStudio, testArtist)).thenReturn(false);

        boolean result = studioMemberService.isMember(1L, 1L);

        assertThat(result).isFalse();
    }

    // isManager

    @Test
    @DisplayName("Проверка является ли пользователь менеджером")
    void isManager_True() {
        StudioMember managerMember = StudioMember.builder()
                .studio(testStudio)
                .member(testArtist)
                .role(StudioRole.MANAGER)
                .build();

        when(studioRepository.findById(1L)).thenReturn(Optional.of(testStudio));
        when(artistProfileRepository.findById(1L)).thenReturn(Optional.of(testArtist));
        when(studioMemberRepository.findByStudioAndMember(testStudio, testArtist))
                .thenReturn(Optional.of(managerMember));

        boolean result = studioMemberService.isManager(1L, 1L);

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("Проверка является ли пользователь менеджером - нет")
    void isManager_False_Artist() {
        when(studioRepository.findById(1L)).thenReturn(Optional.of(testStudio));
        when(artistProfileRepository.findById(1L)).thenReturn(Optional.of(testArtist));
        when(studioMemberRepository.findByStudioAndMember(testStudio, testArtist))
                .thenReturn(Optional.of(testMember)); // role = ARTIST

        boolean result = studioMemberService.isManager(1L, 1L);

        assertThat(result).isFalse();
    }
}