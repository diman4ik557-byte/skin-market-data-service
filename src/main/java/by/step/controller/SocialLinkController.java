package by.step.controller;

import by.step.dto.SocialLinkDto;
import by.step.entity.enums.SocialPlatform;
import by.step.service.SocialLinkService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/social-links")
@RequiredArgsConstructor
public class SocialLinkController {

    private final SocialLinkService socialLinkService;

    @PostMapping("/profile/{profileId}")
    public ResponseEntity<SocialLinkDto> addSocialLink(
            @PathVariable Long profileId,
            @RequestParam SocialPlatform platform,
            @RequestParam String userIdentifier) {
        SocialLinkDto link = socialLinkService.addSocialLink(profileId, platform, userIdentifier);
        return ResponseEntity.ok(link);
    }

    @GetMapping("/profile/{profileId}")
    public ResponseEntity<List<SocialLinkDto>> findByProfile(@PathVariable Long profileId) {
        return ResponseEntity.ok(socialLinkService.findByProfile(profileId));
    }

    @GetMapping("/profile/{profileId}/primary")
    public ResponseEntity<List<SocialLinkDto>> findPrimaryLinks(@PathVariable Long profileId) {
        return ResponseEntity.ok(socialLinkService.findPrimaryLinks(profileId));
    }

    @GetMapping("/profile/{profileId}/platform/{platform}")
    public ResponseEntity<SocialLinkDto> findByProfileAndPlatform(
            @PathVariable Long profileId,
            @PathVariable SocialPlatform platform) {
        return socialLinkService.findByProfileAndPlatform(profileId, platform)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{linkId}/primary")
    public ResponseEntity<Void> setPrimary(@PathVariable Long linkId) {
        socialLinkService.setPrimary(linkId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{linkId}/identifier")
    public ResponseEntity<Void> updateUserIdentifier(
            @PathVariable Long linkId,
            @RequestParam String userIdentifier) {
        socialLinkService.updateUserIdentifier(linkId, userIdentifier);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{linkId}")
    public ResponseEntity<Void> removeSocialLink(@PathVariable Long linkId) {
        socialLinkService.removeSocialLink(linkId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/profile/{profileId}")
    public ResponseEntity<Void> removeAllByProfile(@PathVariable Long profileId) {
        socialLinkService.removeAllByProfile(profileId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/url")
    public ResponseEntity<String> getFullUrl(
            @RequestParam SocialPlatform platform,
            @RequestParam String userIdentifier) {
        return ResponseEntity.ok(socialLinkService.getFullUrl(platform, userIdentifier));
    }
}