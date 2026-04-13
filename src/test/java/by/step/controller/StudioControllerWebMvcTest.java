package by.step.controller;

import by.step.dto.ArtistProfileDto;
import by.step.dto.StudioDto;
import by.step.service.StudioService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(StudioController.class)
@ActiveProfiles("test")
class StudioControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StudioService studioService;

    @Autowired
    private ObjectMapper objectMapper;

    private StudioDto testStudioDto;
    private ArtistProfileDto testArtistDto;
    private List<StudioDto> testStudioList;

    @BeforeEach
    void setUp() {
        testStudioDto = StudioDto.builder()
                .id(1L)
                .profileId(3L)
                .name("Art Masters")
                .description("Студия профессиональных художников по скинам")
                .foundedAt(LocalDate.of(2023, 1, 15))
                .managerId(3L)
                .managerName("maria")
                .membersCount(2)
                .build();

        testArtistDto = ArtistProfileDto.builder()
                .id(1L)
                .profileId(2L)
                .username("petr")
                .styles("классический, реализм")
                .minPrice(BigDecimal.valueOf(1000))
                .averageTime(3)
                .isAvailable(true)
                .build();

        StudioDto studio2 = StudioDto.builder()
                .id(2L)
                .profileId(6L)
                .name("Pixel Masters")
                .description("Студия пиксель-арта")
                .foundedAt(LocalDate.of(2024, 1, 1))
                .managerId(6L)
                .managerName("pixel_artist")
                .membersCount(1)
                .build();

        testStudioList = Arrays.asList(testStudioDto, studio2);
    }

    @Test
    void checkCreateStudio() throws Exception {
        when(studioService.createStudio(eq(1L), anyString(), anyString()))
                .thenReturn(testStudioDto);

        mockMvc.perform(post("/api/studios/user/{userId}", 1L)
                        .param("name", "Art Masters")
                        .param("description", "Студия профессиональных художников по скинам"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Art Masters"))
                .andExpect(jsonPath("$.managerName").value("maria"));
    }

    @Test
    void findById() throws Exception {
        when(studioService.findById(1L)).thenReturn(Optional.of(testStudioDto));

        mockMvc.perform(get("/api/studios/{studioId}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Art Masters"))
                .andExpect(jsonPath("$.membersCount").value(2));
    }

    @Test
    void findByIdNotFound() throws Exception {
        when(studioService.findById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/studios/{studioId}", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void findByUserId() throws Exception {
        when(studioService.findByUserId(3L)).thenReturn(Optional.of(testStudioDto));

        mockMvc.perform(get("/api/studios/user/{userId}", 3L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Art Masters"));
    }

    @Test
    void findAll() throws Exception {
        when(studioService.findAllStudios()).thenReturn(testStudioList);

        mockMvc.perform(get("/api/studios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Art Masters"))
                .andExpect(jsonPath("$[1].name").value("Pixel Masters"));
    }

    @Test
    void findByName() throws Exception {
        List<StudioDto> studios = List.of(testStudioDto);
        when(studioService.findStudiosByName("Art")).thenReturn(studios);

        mockMvc.perform(get("/api/studios/search")
                        .param("name", "Art"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Art Masters"));
    }

    @Test
    void checkUpdateDescription() throws Exception {
        StudioDto updatedStudio = StudioDto.builder()
                .id(1L)
                .name("Art Masters")
                .description("Новое описание студии")
                .build();

        when(studioService.updateDescription(eq(1L), anyString())).thenReturn(updatedStudio);

        mockMvc.perform(put("/api/studios/{studioId}/description", 1L)
                        .param("description", "Новое описание студии"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("Новое описание студии"));
    }

    @Test
    void checkAddMember() throws Exception {
        doNothing().when(studioService).addMember(1L, 5L, "ARTIST");

        mockMvc.perform(post("/api/studios/{studioId}/members/{artistId}", 1L, 5L)
                        .param("role", "ARTIST"))
                .andExpect(status().isOk());

        verify(studioService, times(1)).addMember(1L, 5L, "ARTIST");
    }

    @Test
    void checkRemoveMember() throws Exception {
        doNothing().when(studioService).removeMember(1L, 5L);

        mockMvc.perform(delete("/api/studios/{studioId}/members/{artistId}", 1L, 5L))
                .andExpect(status().isOk());

        verify(studioService, times(1)).removeMember(1L, 5L);
    }

    @Test
    void checkGetMembers() throws Exception {
        List<ArtistProfileDto> members = List.of(testArtistDto);
        when(studioService.getStudioMembers(1L)).thenReturn(members);

        mockMvc.perform(get("/api/studios/{studioId}/members", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].username").value("petr"));
    }

    @Test
    void checkGetMemberCount() throws Exception {
        when(studioService.getMemberCount(1L)).thenReturn(2L);

        mockMvc.perform(get("/api/studios/{studioId}/members/count", 1L))
                .andExpect(status().isOk())
                .andExpect(content().string("2"));
    }

    @Test
    void checkDeleteStudio() throws Exception {
        doNothing().when(studioService).deleteStudio(1L);

        mockMvc.perform(delete("/api/studios/{studioId}", 1L))
                .andExpect(status().isOk());

        verify(studioService, times(1)).deleteStudio(1L);
    }
}