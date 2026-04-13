package by.step.controller;

import by.step.dto.SocialLinkDto;
import by.step.entity.enums.SocialPlatform;
import by.step.service.SocialLinkService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SocialLinkController.class)
@ActiveProfiles("test")
class SocialLinkControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SocialLinkService socialLinkService;

    @Autowired
    private ObjectMapper objectMapper;

    private SocialLinkDto testLinkDto;
    private SocialLinkDto primaryLinkDto;
    private List<SocialLinkDto> testLinkList;

    @BeforeEach
    void setUp() {
        testLinkDto = SocialLinkDto.builder()
                .id(1L)
                .profileId(2L)
                .platform(SocialPlatform.VK)
                .platformDisplayName("ВКонтакте")
                .userIdentifier("petr_artist")
                .fullUrl("https://vk.com/petr_artist")
                .isPrimary(false)
                .build();

        primaryLinkDto = SocialLinkDto.builder()
                .id(1L)
                .profileId(2L)
                .platform(SocialPlatform.VK)
                .platformDisplayName("ВКонтакте")
                .userIdentifier("petr_artist")
                .fullUrl("https://vk.com/petr_artist")
                .isPrimary(true)
                .build();

        SocialLinkDto discordLink = SocialLinkDto.builder()
                .id(2L)
                .profileId(2L)
                .platform(SocialPlatform.DISCORD)
                .platformDisplayName("Discord")
                .userIdentifier("petr#1234")
                .fullUrl("https://discord.gg/petr#1234")
                .isPrimary(false)
                .build();

        testLinkList = Arrays.asList(testLinkDto, discordLink);
    }

    @Test
    void checkAddSocialLink() throws Exception {
        when(socialLinkService.addSocialLink(eq(2L), eq(SocialPlatform.VK), eq("petr_artist")))
                .thenReturn(testLinkDto);

        mockMvc.perform(post("/api/social-links/profile/{profileId}", 2L)
                        .param("platform", "VK")
                        .param("userIdentifier", "petr_artist"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.platform").value("VK"))
                .andExpect(jsonPath("$.userIdentifier").value("petr_artist"));
    }

    @Test
    void findByProfile() throws Exception {
        when(socialLinkService.findByProfile(2L)).thenReturn(testLinkList);

        mockMvc.perform(get("/api/social-links/profile/{profileId}", 2L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].platform").value("VK"))
                .andExpect(jsonPath("$[1].platform").value("DISCORD"));
    }

    @Test
    void findPrimaryLinks() throws Exception {
        List<SocialLinkDto> primaryLinks = List.of(primaryLinkDto);
        when(socialLinkService.findPrimaryLinks(2L)).thenReturn(primaryLinks);

        mockMvc.perform(get("/api/social-links/profile/{profileId}/primary", 2L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].isPrimary").value(true));
    }

    @Test
    void findByProfileAndPlatform() throws Exception {
        when(socialLinkService.findByProfileAndPlatform(eq(2L), eq(SocialPlatform.VK)))
                .thenReturn(Optional.of(testLinkDto));

        mockMvc.perform(get("/api/social-links/profile/{profileId}/platform/{platform}", 2L, "VK"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.platform").value("VK"))
                .andExpect(jsonPath("$.userIdentifier").value("petr_artist"));
    }

    @Test
    void findByProfileAndPlatformNotFound() throws Exception {
        when(socialLinkService.findByProfileAndPlatform(eq(2L), eq(SocialPlatform.INSTAGRAM)))
                .thenReturn(Optional.empty());

        mockMvc.perform(get("/api/social-links/profile/{profileId}/platform/{platform}", 2L, "INSTAGRAM"))
                .andExpect(status().isNotFound());
    }

    @Test
    void checkSetPrimary() throws Exception {
        doNothing().when(socialLinkService).setPrimary(1L);

        mockMvc.perform(put("/api/social-links/{linkId}/primary", 1L))
                .andExpect(status().isOk());

        verify(socialLinkService, times(1)).setPrimary(1L);
    }

    @Test
    void checkUpdateUserIdentifier() throws Exception {
        doNothing().when(socialLinkService).updateUserIdentifier(1L, "new_petr_artist");

        mockMvc.perform(put("/api/social-links/{linkId}/identifier", 1L)
                        .param("userIdentifier", "new_petr_artist"))
                .andExpect(status().isOk());

        verify(socialLinkService, times(1)).updateUserIdentifier(1L, "new_petr_artist");
    }

    @Test
    void checkRemoveSocialLink() throws Exception {
        doNothing().when(socialLinkService).removeSocialLink(1L);

        mockMvc.perform(delete("/api/social-links/{linkId}", 1L))
                .andExpect(status().isOk());

        verify(socialLinkService, times(1)).removeSocialLink(1L);
    }

    @Test
    void checkRemoveAllByProfile() throws Exception {
        doNothing().when(socialLinkService).removeAllByProfile(2L);

        mockMvc.perform(delete("/api/social-links/profile/{profileId}", 2L))
                .andExpect(status().isOk());

        verify(socialLinkService, times(1)).removeAllByProfile(2L);
    }

    @Test
    void checkGetFullUrl() throws Exception {
        String expectedUrl = "https://vk.com/test_user";
        when(socialLinkService.getFullUrl(SocialPlatform.VK, "test_user"))
                .thenReturn(expectedUrl);

        mockMvc.perform(get("/api/social-links/url")
                        .param("platform", "VK")
                        .param("userIdentifier", "test_user"))
                .andExpect(status().isOk())
                .andExpect(content().string(expectedUrl));
    }
}