package by.step.controller;

import by.step.dto.ArtistProfileDto;
import by.step.dto.StudioDto;
import by.step.service.StudioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/studios")
@RequiredArgsConstructor
public class StudioController {

    private final StudioService studioService;

    @PostMapping("/user/{userId}")
    public ResponseEntity<StudioDto> createStudio(
            @PathVariable Long userId,
            @RequestParam String name,
            @RequestParam String description) {
        StudioDto studio = studioService.createStudio(userId, name, description);
        return ResponseEntity.ok(studio);
    }

    @GetMapping("/{studioId}")
    public ResponseEntity<StudioDto> findById(@PathVariable Long studioId) {
        return studioService.findById(studioId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<StudioDto> findByUserId(@PathVariable Long userId) {
        return studioService.findByUserId(userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<StudioDto>> findAll() {
        return ResponseEntity.ok(studioService.findAllStudios());
    }

    @GetMapping("/search")
    public ResponseEntity<List<StudioDto>> findByName(@RequestParam String name) {
        return ResponseEntity.ok(studioService.findStudiosByName(name));
    }

    @PutMapping("/{studioId}/description")
    public ResponseEntity<StudioDto> updateDescription(
            @PathVariable Long studioId,
            @RequestParam String description) {
        StudioDto studio = studioService.updateDescription(studioId, description);
        return ResponseEntity.ok(studio);
    }

    @PostMapping("/{studioId}/members/{artistId}")
    public ResponseEntity<Void> addMember(
            @PathVariable Long studioId,
            @PathVariable Long artistId,
            @RequestParam String role) {
        studioService.addMember(studioId, artistId, role);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{studioId}/members/{artistId}")
    public ResponseEntity<Void> removeMember(
            @PathVariable Long studioId,
            @PathVariable Long artistId) {
        studioService.removeMember(studioId, artistId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{studioId}/members")
    public ResponseEntity<List<ArtistProfileDto>> getMembers(@PathVariable Long studioId) {
        return ResponseEntity.ok(studioService.getStudioMembers(studioId));
    }

    @GetMapping("/{studioId}/members/count")
    public ResponseEntity<Long> getMemberCount(@PathVariable Long studioId) {
        return ResponseEntity.ok(studioService.getMemberCount(studioId));
    }

    @DeleteMapping("/{studioId}")
    public ResponseEntity<Void> deleteStudio(@PathVariable Long studioId) {
        studioService.deleteStudio(studioId);
        return ResponseEntity.ok().build();
    }
}