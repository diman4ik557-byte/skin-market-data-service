package by.step.controller;

import by.step.dto.ArtistProfileDto;
import by.step.dto.StudioDto;
import by.step.service.StudioService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(StudioController.class)
@DisplayName("Web тесты контроллера студий")
class StudioControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StudioService studioService;

    @Autowired
    private ObjectMapper objectMapper;

    private StudioDto testStudioDto;
    private ArtistProfileDto testArtistDto;

    @BeforeEach
    void setUp() {
        testStudioDto = StudioDto.builder()
                .id(1L)
                .profileId(1L)
                .name("Test Studio")
                .description("Test Description")
                .foundedAt(LocalDate.now())
                .managerId(1L)
                .managerName("testuser")
                .membersCount(0)
                .build();

        testArtistDto = ArtistProfileDto.builder()
                .id(1L)
                .profileId(1L)
                .username("testuser")
                .styles("classic")
                .minPrice(BigDecimal.valueOf(500))
                .averageTime(3)
                .isAvailable(true)
                .build();
    }

    @Test
    @DisplayName("Создание студии - успех")
    void createStudio_Success() throws Exception {
        when(studioService.createStudio(anyLong(), anyString(), anyString()))
                .thenReturn(testStudioDto);

        mockMvc.perform(post("/api/studios")
                        .param("userId", "1")
                        .param("name", "Test Studio")
                        .param("description", "Test Description"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Test Studio"));
    }

    @Test
    @DisplayName("Получение студии по ID - успех")
    void getStudioById_Success() throws Exception {
        when(studioService.findById(1L)).thenReturn(Optional.of(testStudioDto));

        mockMvc.perform(get("/api/studios/{studioId}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Test Studio"));
    }

    @Test
    @DisplayName("Получение студии по ID - не найдена")
    void getStudioById_NotFound() throws Exception {
        when(studioService.findById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/studios/{studioId}", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Получение всех студий")
    void getAllStudios_Success() throws Exception {
        List<StudioDto> studios = Collections.singletonList(testStudioDto);
        when(studioService.findAllStudios()).thenReturn(studios);

        mockMvc.perform(get("/api/studios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Test Studio"));
    }

    @Test
    @DisplayName("Получение участников студии")
    void getStudioMembers_Success() throws Exception {
        List<ArtistProfileDto> members = Collections.singletonList(testArtistDto);
        when(studioService.getStudioMembers(1L)).thenReturn(members);

        mockMvc.perform(get("/api/studios/{studioId}/members", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].username").value("testuser"));
    }

    @Test
    @DisplayName("Подача заявки на вступление")
    void requestToJoinStudio_Success() throws Exception {
        doNothing().when(studioService).requestToJoinStudio(1L, 2L);

        mockMvc.perform(post("/api/studios/{studioId}/members/{artistId}/request", 1L, 2L))
                .andExpect(status().isOk());

        verify(studioService, times(1)).requestToJoinStudio(1L, 2L);
    }

    @Test
    @DisplayName("Одобрение заявки")
    void approveMember_Success() throws Exception {
        doNothing().when(studioService).approveMember(1L, 2L, 1L);

        mockMvc.perform(post("/api/studios/{studioId}/members/{artistId}/approve", 1L, 2L)
                        .param("managerId", "1"))
                .andExpect(status().isOk());

        verify(studioService, times(1)).approveMember(1L, 2L, 1L);
    }

    @Test
    @DisplayName("Удаление участника")
    void removeMember_Success() throws Exception {
        doNothing().when(studioService).removeMember(1L, 2L, 1L);

        mockMvc.perform(delete("/api/studios/{studioId}/members/{artistId}", 1L, 2L)
                        .param("managerId", "1"))
                .andExpect(status().isOk());

        verify(studioService, times(1)).removeMember(1L, 2L, 1L);
    }

    @Test
    @DisplayName("Выход из студии")
    void leaveStudio_Success() throws Exception {
        doNothing().when(studioService).leaveStudio(1L, 2L);

        mockMvc.perform(post("/api/studios/{studioId}/members/{artistId}/leave", 1L, 2L))
                .andExpect(status().isOk());

        verify(studioService, times(1)).leaveStudio(1L, 2L);
    }

    @Test
    @DisplayName("Обновление описания студии")
    void updateStudio_Success() throws Exception {
        when(studioService.updateDescription(eq(1L), anyString(), eq(1L)))
                .thenReturn(testStudioDto);

        mockMvc.perform(put("/api/studios/{studioId}", 1L)
                        .param("description", "New Description")
                        .param("managerId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("Test Description"));
    }

    @Test
    @DisplayName("Удаление студии")
    void deleteStudio_Success() throws Exception {
        doNothing().when(studioService).deleteStudio(1L, 1L);

        mockMvc.perform(delete("/api/studios/{studioId}", 1L)
                        .param("managerId", "1"))
                .andExpect(status().isOk());

        verify(studioService, times(1)).deleteStudio(1L, 1L);
    }

    @Test
    @DisplayName("Проверка прав менеджера")
    void isManager_Success() throws Exception {
        when(studioService.isManager(1L, 1L)).thenReturn(true);

        mockMvc.perform(get("/api/studios/{studioId}/is-manager", 1L)
                        .param("userId", "1"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }
}