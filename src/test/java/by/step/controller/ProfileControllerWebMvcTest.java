package by.step.controller;

import by.step.dto.ProfileDto;
import by.step.service.ProfileService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProfileController.class)
@ActiveProfiles("test")
class ProfileControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProfileService profileService;

    @Autowired
    private ObjectMapper objectMapper;

    private ProfileDto testProfileDto;

    @BeforeEach
    void setUp() {
        testProfileDto = ProfileDto.builder()
                .id(1L)
                .userId(1L)
                .username("testuser")
                .bio("Тестовый профиль")
                .isArtist(false)
                .isStudio(false)
                .build();
    }

    @Test
    void checkCreateProfile() throws Exception {
        when(profileService.createProfile(1L)).thenReturn(testProfileDto);

        mockMvc.perform(post("/api/profiles/user/{userId}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.username").value("testuser"));
    }

    @Test
    void findByUserId() throws Exception {
        when(profileService.findByUserId(1L)).thenReturn(Optional.of(testProfileDto));

        mockMvc.perform(get("/api/profiles/user/{userId}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.bio").value("Тестовый профиль"));
    }

    @Test
    void findByUserIdNotFound() throws Exception {
        when(profileService.findByUserId(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/profiles/user/{userId}", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void checkUpdateBio() throws Exception {
        ProfileDto updatedProfile = ProfileDto.builder()
                .id(1L)
                .userId(1L)
                .username("testuser")
                .bio("Новое био")
                .build();

        when(profileService.updateBio(eq(1L), any(String.class))).thenReturn(updatedProfile);

        mockMvc.perform(put("/api/profiles/{profileId}/bio", 1L)
                        .param("bio", "Новое био"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bio").value("Новое био"));
    }

    @Test
    void checkSetArtistStatus() throws Exception {
        doNothing().when(profileService).setArtistStatus(1L, true);

        mockMvc.perform(put("/api/profiles/{profileId}/artist", 1L)
                        .param("isArtist", "true"))
                .andExpect(status().isOk());

        verify(profileService, times(1)).setArtistStatus(1L, true);
    }

    @Test
    void checkSetStudioStatus() throws Exception {
        doNothing().when(profileService).setStudioStatus(1L, true);

        mockMvc.perform(put("/api/profiles/{profileId}/studio", 1L)
                        .param("isStudio", "true"))
                .andExpect(status().isOk());

        verify(profileService, times(1)).setStudioStatus(1L, true);
    }

    @Test
    void checkIsArtist() throws Exception {
        when(profileService.isArtist(1L)).thenReturn(true);

        mockMvc.perform(get("/api/profiles/user/{userId}/is-artist", 1L))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    void checkIsStudio() throws Exception {
        when(profileService.isStudio(1L)).thenReturn(true);

        mockMvc.perform(get("/api/profiles/user/{userId}/is-studio", 1L))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }
}