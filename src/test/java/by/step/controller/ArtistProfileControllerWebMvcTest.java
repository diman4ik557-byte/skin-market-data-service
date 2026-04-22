package by.step.controller;

import by.step.dto.ArtistProfileDto;
import by.step.service.ArtistProfileService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ArtistProfileController.class)
@ActiveProfiles("test")
class ArtistProfileControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ArtistProfileService artistProfileService;

    @Autowired
    private ObjectMapper objectMapper;

    private ArtistProfileDto testArtistDto;
    private List<ArtistProfileDto> testArtistList;

    @BeforeEach
    void setUp() {
        testArtistDto = ArtistProfileDto.builder()
                .id(1L)
                .profileId(1L)
                .username("testartist")
                .styles("классический, реализм")
                .minPrice(BigDecimal.valueOf(1000))
                .averageTime(3)
                .isAvailable(true)
                .build();

        ArtistProfileDto artist2 = ArtistProfileDto.builder()
                .id(2L)
                .profileId(2L)
                .username("artist2")
                .styles("Cel-shading, Flat")
                .minPrice(BigDecimal.valueOf(800))
                .averageTime(2)
                .isAvailable(true)
                .build();

        testArtistList = Arrays.asList(testArtistDto, artist2);
    }

    @Test
    void checkCreateArtistProfile() throws Exception {
        when(artistProfileService.createArtistProfile(eq(1L), anyString(), any(BigDecimal.class), anyInt()))
                .thenReturn(testArtistDto);

        mockMvc.perform(post("/api/artist-profiles/user/{userId}", 1L)
                        .param("styles", "классический, реализм")
                        .param("minPrice", "1000")
                        .param("averageTime", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("testartist"))
                .andExpect(jsonPath("$.styles").value("классический, реализм"));
    }

    @Test
    void checkFindByUserId() throws Exception {
        when(artistProfileService.findByUserId(1L)).thenReturn(Optional.of(testArtistDto));

        mockMvc.perform(get("/api/artist-profiles/user/{userId}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testartist"))
                .andExpect(jsonPath("$.minPrice").value(1000));
    }

    @Test
    void checkFindByUserIdNotFound() throws Exception {
        when(artistProfileService.findByUserId(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/artist-profiles/user/{userId}", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void checkFindAll() throws Exception {
        when(artistProfileService.findAllArtists()).thenReturn(testArtistList);

        mockMvc.perform(get("/api/artist-profiles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].username").value("testartist"))
                .andExpect(jsonPath("$[1].username").value("artist2"));
    }

    @Test
    void checkFindAvailable() throws Exception {
        when(artistProfileService.findAvailableArtists()).thenReturn(testArtistList);

        mockMvc.perform(get("/api/artist-profiles/available"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void checkFindByStyle() throws Exception {
        List<ArtistProfileDto> artistsByStyle = List.of(testArtistDto);
        when(artistProfileService.findArtistsByStyle("классический")).thenReturn(artistsByStyle);

        mockMvc.perform(get("/api/artist-profiles/style/{style}", "классический"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].styles").value("классический, реализм"));
    }

    @Test
    void checkFindByMaxPrice() throws Exception {
        when(artistProfileService.findArtistsByMaxPrice(BigDecimal.valueOf(900)))
                .thenReturn(List.of(testArtistDto));

        mockMvc.perform(get("/api/artist-profiles/max-price/{maxPrice}", 900))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void checkUpdateStyles() throws Exception {
        ArtistProfileDto updatedArtist = ArtistProfileDto.builder()
                .id(1L)
                .username("testartist")
                .styles("новый стиль")
                .build();

        when(artistProfileService.updateStyles(eq(1L), anyString())).thenReturn(updatedArtist);

        mockMvc.perform(put("/api/artist-profiles/{artistId}/styles", 1L)
                        .param("styles", "новый стиль"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.styles").value("новый стиль"));
    }

    @Test
    void checkUpdateMinPrice() throws Exception {
        ArtistProfileDto updatedArtist = ArtistProfileDto.builder()
                .id(1L)
                .username("testartist")
                .minPrice(BigDecimal.valueOf(2000))
                .build();

        when(artistProfileService.updateMinPrice(eq(1L), any(BigDecimal.class))).thenReturn(updatedArtist);

        mockMvc.perform(put("/api/artist-profiles/{artistId}/min-price", 1L)
                        .param("minPrice", "2000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.minPrice").value(2000));
    }

    @Test
    void checkUpdateAvailability() throws Exception {
        doNothing().when(artistProfileService).updateAvailability(1L, false);

        mockMvc.perform(put("/api/artist-profiles/{artistId}/availability", 1L)
                        .param("isAvailable", "false"))
                .andExpect(status().isOk());

        verify(artistProfileService, times(1)).updateAvailability(1L, false);
    }

    @Test
    void checkAssignToStudio() throws Exception {
        doNothing().when(artistProfileService).assignToStudio(1L, 5L);

        mockMvc.perform(post("/api/artist-profiles/{artistId}/studio/{studioId}", 1L, 5L))
                .andExpect(status().isOk());

        verify(artistProfileService, times(1)).assignToStudio(1L, 5L);
    }

    @Test
    void checkRemoveFromStudio() throws Exception {
        doNothing().when(artistProfileService).removeFromStudio(1L);

        mockMvc.perform(delete("/api/artist-profiles/{artistId}/studio", 1L))
                .andExpect(status().isOk());

        verify(artistProfileService, times(1)).removeFromStudio(1L);
    }
}