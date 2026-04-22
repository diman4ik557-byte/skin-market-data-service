package by.step.controller;

import by.step.dto.ProfileDto;
import by.step.service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profiles")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    @PostMapping("/user/{userId}")
    public ResponseEntity<ProfileDto> createProfile(@PathVariable Long userId) {
        ProfileDto profile = profileService.createProfile(userId);
        return ResponseEntity.ok(profile);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ProfileDto> findByUserId(@PathVariable Long userId) {
        return profileService.findByUserId(userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{profileId}/bio")
    public ResponseEntity<ProfileDto> updateBio(
            @PathVariable Long profileId,
            @RequestParam String bio) {
        ProfileDto profile = profileService.updateBio(profileId, bio);
        return ResponseEntity.ok(profile);
    }

    @PutMapping("/{profileId}/artist")
    public ResponseEntity<Void> setArtistStatus(
            @PathVariable Long profileId,
            @RequestParam boolean isArtist) {
        profileService.setArtistStatus(profileId, isArtist);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{profileId}/studio")
    public ResponseEntity<Void> setStudioStatus(
            @PathVariable Long profileId,
            @RequestParam boolean isStudio) {
        profileService.setStudioStatus(profileId, isStudio);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/user/{userId}/is-artist")
    public ResponseEntity<Boolean> isArtist(@PathVariable Long userId) {
        return ResponseEntity.ok(profileService.isArtist(userId));
    }

    @GetMapping("/user/{userId}/is-studio")
    public ResponseEntity<Boolean> isStudio(@PathVariable Long userId) {
        return ResponseEntity.ok(profileService.isStudio(userId));
    }
}
